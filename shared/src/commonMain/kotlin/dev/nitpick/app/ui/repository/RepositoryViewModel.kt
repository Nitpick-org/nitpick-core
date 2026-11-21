package dev.nitpick.app.ui.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.git.GitRepositoryFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RepositoryUiState(
    val path: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentBranch: String = "",
    val commits: List<Commit> = emptyList(),
    val refs: List<Ref> = emptyList(),
)

class RepositoryViewModel(
    private val gitRepositoryFactory: GitRepositoryFactory,
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
                RepositoryUiState(
                    path = path,
                    currentBranch = repository.currentBranch(),
                    commits = repository.log(),
                    refs = repository.refs(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RepositoryUiState(path = path, errorMessage = e.message ?: e.toString())
            }
        }
    }
}
