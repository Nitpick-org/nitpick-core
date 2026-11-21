package dev.nitpick.app.ui.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.git.GitRepository
import dev.nitpick.app.git.GitRepositoryFactory
import dev.nitpick.app.review.LocalReviewAgent
import dev.nitpick.app.review.ReviewAgent
import dev.nitpick.app.review.ReviewAnchoring
import dev.nitpick.app.graph.CommitGraphRow
import dev.nitpick.app.graph.buildCommitGraph
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RepositoryUiState(
    val path: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentBranch: String = "",
    val graph: List<CommitGraphRow> = emptyList(),
    val refs: List<Ref> = emptyList(),
    val selectedCommitId: String? = null,
    val baseRef: String = "main",
    val diff: List<FileDiff> = emptyList(),
    val isDiffLoading: Boolean = false,
    val diffError: String? = null,
    val selectedDiffPath: String? = null,
    val review: ReviewResult? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null,
    val appliedIds: Set<String> = emptySet(),
    val appliedFiles: Set<String> = emptySet(),
    val commitMessage: String = "",
) {
    val selectedCommit: Commit?
        get() = graph.firstOrNull { it.commit.id == selectedCommitId }?.commit

    // Falls back to the first file, so a reload that drops the selected file still shows one.
    val selectedDiff: FileDiff?
        get() = diff.firstOrNull { it.path == selectedDiffPath } ?: diff.firstOrNull()
}

/**
 * [graphDispatcher] runs the lane layout off the UI thread; tests pass their own dispatcher
 * so the result is ready when they assert.
 */
class RepositoryViewModel(
    private val gitRepositoryFactory: GitRepositoryFactory,
    private val graphDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val reviewAgent: ReviewAgent = LocalReviewAgent(),
) : ViewModel() {

    private val _state = MutableStateFlow(RepositoryUiState())
    val state: StateFlow<RepositoryUiState> = _state.asStateFlow()

    private var repository: GitRepository? = null
    private var loading: Job? = null
    private var diffLoading: Job? = null

    fun openRepository(path: String) {
        // Opening another folder while one is loading must not let the old result win.
        loading?.cancel()
        diffLoading?.cancel()
        repository = null
        _state.value = RepositoryUiState(path = path, isLoading = true)
        loading = viewModelScope.launch {
            _state.value = try {
                val opened = gitRepositoryFactory.open(path)
                val commits = opened.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                repository = opened
                RepositoryUiState(
                    path = path,
                    currentBranch = opened.currentBranch(),
                    graph = graph,
                    refs = opened.refs(),
                    selectedCommitId = graph.firstOrNull()?.commit?.id,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RepositoryUiState(path = path, errorMessage = e.message ?: e.toString())
            }
            refreshDiff()
        }
    }

    fun selectCommit(commitId: String) {
        _state.update { it.copy(selectedCommitId = commitId) }
    }

    fun selectBase(baseRef: String) {
        _state.update { it.copy(baseRef = baseRef) }
        refreshDiff()
    }

    fun selectDiffFile(path: String) {
        _state.update { it.copy(selectedDiffPath = path) }
    }

    /** Reloads the diff, which also picks up edits made in the working tree since the last load. */
    fun refreshDiff() {
        val git = repository ?: return
        val baseRef = _state.value.baseRef
        diffLoading?.cancel()
        _state.update { it.copy(isDiffLoading = true, diffError = null) }
        diffLoading = viewModelScope.launch {
            try {
                val diff = git.diffAgainst(baseRef)
                _state.update { it.copy(diff = diff, isDiffLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(diff = emptyList(), isDiffLoading = false, diffError = e.message ?: e.toString())
                }
            }
        }
    }

    /** Runs the review agent against the current branch and base ref. */
    fun reviewBranch() {
        val git = repository ?: return
        if (_state.value.isReviewing) return
        _state.update { it.copy(isReviewing = true, reviewError = null) }
        viewModelScope.launch {
            try {
                val result = reviewAgent.review(git, _state.value.baseRef)
                _state.update { it.copy(review = result, isReviewing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isReviewing = false, reviewError = e.message ?: e.toString()) }
            }
        }
    }

    fun applySuggestion(comment: ReviewComment) {
        val git = repository ?: return
        viewModelScope.launch {
            try {
                val content = git.readFile(comment.filePath)
                    ?: error("File not found: ${comment.filePath}")
                val updated = ReviewAnchoring.apply(content, comment)
                git.writeFile(comment.filePath, updated)
                _state.update {
                    it.copy(
                        appliedIds = it.appliedIds + comment.id,
                        appliedFiles = it.appliedFiles + comment.filePath,
                        commitMessage = it.commitMessage.ifBlank { "Apply review suggestions" },
                    )
                }
                refreshDiff()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(reviewError = e.message ?: e.toString()) }
            }
        }
    }

    fun dismissSuggestion(commentId: String) {
        _state.update { state ->
            state.copy(review = state.review?.copy(comments = state.review.comments.filterNot { it.id == commentId }))
        }
    }

    fun setCommitMessage(message: String) {
        _state.update { it.copy(commitMessage = message) }
    }

    fun commitAppliedSuggestions() {
        val git = repository ?: return
        val state = _state.value
        if (state.appliedFiles.isEmpty()) return
        viewModelScope.launch {
            try {
                git.stage(state.appliedFiles.toList())
                git.commit(state.commitMessage)
                val commits = git.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                _state.update {
                    it.copy(
                        graph = graph,
                        refs = git.refs(),
                        selectedCommitId = graph.firstOrNull()?.commit?.id,
                        review = null,
                        reviewError = null,
                        appliedIds = emptySet(),
                        appliedFiles = emptySet(),
                        commitMessage = "",
                    )
                }
                refreshDiff()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(reviewError = e.message ?: e.toString()) }
            }
        }
    }
}
