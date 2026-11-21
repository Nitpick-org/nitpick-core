package dev.nitpick.app.ui.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.git.GitRepositoryFactory
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
) : ViewModel() {

    private val _state = MutableStateFlow(RepositoryUiState())
    val state: StateFlow<RepositoryUiState> = _state.asStateFlow()

    private var loading: Job? = null

    fun openRepository(path: String) {
        // Opening another folder while one is loading must not let the old result win.
        loading?.cancel()
        _state.value = RepositoryUiState(path = path, isLoading = true)
        loading = viewModelScope.launch {
            _state.value = try {
                val repository = gitRepositoryFactory.open(path)
                val commits = repository.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                RepositoryUiState(
                    path = path,
                    currentBranch = repository.currentBranch(),
                    graph = graph,
                    refs = repository.refs(),
                    selectedCommitId = graph.firstOrNull()?.commit?.id,
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
}
