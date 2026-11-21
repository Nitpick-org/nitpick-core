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
    val review: ReviewResult? = null,
    val isReviewing: Boolean = false,
    val reviewError: String? = null,
    val appliedIds: Set<String> = emptySet(),
    val appliedFiles: Set<String> = emptySet(),
    val commitMessage: String = "",
) {
    val selectedCommit: Commit?
        get() = graph.firstOrNull { it.commit.id == selectedCommitId }?.commit
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

    private var loading: Job? = null
    private var repository: GitRepository? = null

    fun openRepository(path: String) {
        // Opening another folder while one is loading must not let the old result win.
        loading?.cancel()
        _state.value = RepositoryUiState(path = path, isLoading = true)
        loading = viewModelScope.launch {
            _state.value = try {
                val opened = gitRepositoryFactory.open(path)
                repository = opened
                val commits = opened.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                val diff = runCatching { opened.diffAgainst("main") }.getOrDefault(emptyList())
                RepositoryUiState(
                    path = path,
                    currentBranch = opened.currentBranch(),
                    graph = graph,
                    refs = opened.refs(),
                    selectedCommitId = graph.firstOrNull()?.commit?.id,
                    diff = diff,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RepositoryUiState(path = path, errorMessage = e.message ?: e.toString())
            }
        }
    }

    fun selectCommit(commitId: String) {
        _state.update { it.copy(selectedCommitId = commitId) }
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
                val diff = runCatching { git.diffAgainst(_state.value.baseRef) }
                    .getOrDefault(_state.value.diff)
                _state.update {
                    it.copy(
                        diff = diff,
                        appliedIds = it.appliedIds + comment.id,
                        appliedFiles = it.appliedFiles + comment.filePath,
                        commitMessage = it.commitMessage.ifBlank { "Apply review suggestions" },
                    )
                }
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
                val diff = runCatching { git.diffAgainst(state.baseRef) }.getOrDefault(emptyList())
                _state.update {
                    it.copy(
                        graph = graph,
                        refs = git.refs(),
                        selectedCommitId = graph.firstOrNull()?.commit?.id,
                        diff = diff,
                        review = null,
                        reviewError = null,
                        appliedIds = emptySet(),
                        appliedFiles = emptySet(),
                        commitMessage = "",
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(reviewError = e.message ?: e.toString()) }
            }
        }
    }
}
