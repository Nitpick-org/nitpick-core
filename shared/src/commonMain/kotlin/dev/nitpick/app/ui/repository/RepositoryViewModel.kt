package dev.nitpick.app.ui.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.domain.TrackingStatus
import dev.nitpick.app.git.GitRepository
import dev.nitpick.app.git.GitRepositoryFactory
import dev.nitpick.app.review.LocalReviewAgent
import dev.nitpick.app.review.MissingApiKeyException
import dev.nitpick.app.review.ReviewAgent
import dev.nitpick.app.review.ReviewAnchoring
import dev.nitpick.app.review.ReviewProviderException
import dev.nitpick.app.review.ReviewToolUse
import dev.nitpick.app.share.ReviewShareClient
import dev.nitpick.app.share.ReviewShareException
import dev.nitpick.app.share.withoutCredentials
import dev.nitpick.app.graph.CommitGraphRow
import dev.nitpick.app.graph.buildCommitGraph
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import dev.nitpick.app.settings.LlmProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// The handoff promises an answer within 90 s; past that the user gets a Retry, not a spinner.
internal val REVIEW_TIMEOUT = 90.seconds

sealed interface ReviewFailure {
    data class MissingKey(val provider: LlmProvider) : ReviewFailure
    data object Network : ReviewFailure
    data object Timeout : ReviewFailure
    data class Other(val message: String) : ReviewFailure
}

sealed interface ShareFailure {
    /** No `origin` remote: nothing identifies the repository on another device. */
    data object NoRemote : ShareFailure
    data object Unreachable : ShareFailure
}

private fun Throwable.toReviewFailure(): ReviewFailure = when {
    this is MissingApiKeyException -> ReviewFailure.MissingKey(provider)
    isTimeout() -> ReviewFailure.Timeout
    this is ReviewProviderException -> ReviewFailure.Network
    else -> ReviewFailure.Other(message ?: toString())
}

private fun Throwable.isTimeout(): Boolean {
    val text = generateSequence(this as Throwable?) { it.cause }
        .mapNotNull { it.message }
        .joinToString(" ")
        .lowercase()
    return "timeout" in text || "timed out" in text
}

/** The clone into [path]: running while [errorMessage] is null. */
data class CloneStatus(val path: String, val errorMessage: String? = null)

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
    val reviewError: ReviewFailure? = null,
    // The guarded tool calls behind [review] (names and paths only), shown in the Review tab (#80).
    val reviewToolUses: List<ReviewToolUse> = emptyList(),
    val actionError: String? = null,
    val appliedIds: Set<String> = emptySet(),
    val appliedFiles: Set<String> = emptySet(),
    val commitMessage: String = "",
    val isPushing: Boolean = false,
    val pushError: String? = null,
    val isCheckingOut: Boolean = false,
    val checkoutError: String? = null,
    val isPulling: Boolean = false,
    val pullError: String? = null,
    val lastPull: PullResult? = null,
    val isSharing: Boolean = false,
    // Set once the review on screen is on the server: shared from here, or opened from the list.
    val sharedReviewId: String? = null,
    val shareFailure: ShareFailure? = null,
    val sharedReviews: List<SharedReview> = emptyList(),
    val isLoadingSharedReviews: Boolean = false,
    val sharedReviewsFailure: ShareFailure? = null,
    val openedSharedReview: SharedReview? = null,
    val isFetching: Boolean = false,
    val fetchError: String? = null,
    val tracking: TrackingStatus? = null,
    // A clone in progress or failed; the open repository stays on screen until the clone succeeds.
    val clone: CloneStatus? = null,
) {
    /** One git operation at a time: pull, push and checkout share the same working tree. */
    val isSyncing: Boolean get() = isPulling || isPushing || isCheckingOut || isFetching

    /** Local commits the origin does not have yet: true when the current branch is ahead of, or unknown to, origin. */
    val hasUnpushedCommits: Boolean
        get() {
            tracking?.let { return it.ahead > 0 }
            val local = refs.firstOrNull { it.type == RefType.LOCAL_BRANCH && it.name == currentBranch } ?: return false
            val remote = refs.firstOrNull { it.type == RefType.REMOTE_BRANCH && it.name == "origin/$currentBranch" }
            return remote?.commitId != local.commitId
        }

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
    private val shareClient: ReviewShareClient? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(RepositoryUiState())
    val state: StateFlow<RepositoryUiState> = _state.asStateFlow()

    private var repository: GitRepository? = null
    private var loading: Job? = null
    private var cloning: Job? = null
    private var diffJob: Job? = null
    // A refresh asked while a diff is running (apply, tab switch) must not start a second
    // JGit walk: remember the newest base and let the running job loop once more.
    private var pendingDiffBase: String? = null
    private var sharedLoading: Job? = null

    // Two applies on the same file must not read the same original and overwrite each other.
    private val writeLock = Mutex()

    fun openRepository(path: String) = load(path) { gitRepositoryFactory.open(path) }

    /** Clones [url] into [path]; the token in [credentials] stays in memory and never reaches the state. */
    fun cloneRepository(url: String, path: String, credentials: Credentials?) {
        cloning?.cancel()
        _state.update { it.copy(clone = CloneStatus(path)) }
        cloning = viewModelScope.launch {
            val cloned = try {
                gitRepositoryFactory.clone(url, path, credentials)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(clone = CloneStatus(path, e.message ?: e.toString())) }
                return@launch
            }
            load(path) { cloned }
        }
    }

    private fun load(path: String, openRepository: suspend () -> GitRepository) {
        // Opening another folder while one is loading must not let the old result win.
        loading?.cancel()
        diffJob?.cancel()
        pendingDiffBase = null
        repository = null
        _state.value = RepositoryUiState(path = path, isLoading = true)
        loading = viewModelScope.launch {
            _state.value = try {
                val opened = openRepository()
                val commits = opened.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                repository = opened
                val refs = opened.refs()
                RepositoryUiState(
                    path = path,
                    currentBranch = opened.currentBranch(),
                    graph = graph,
                    refs = refs,
                    selectedCommitId = graph.firstOrNull()?.commit?.id,
                    baseRef = defaultBaseRef(refs),
                    tracking = opened.trackingStatus(),
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
        pendingDiffBase = _state.value.baseRef
        if (diffJob?.isActive == true) return
        diffJob = viewModelScope.launch {
            while (true) {
                val baseRef = pendingDiffBase ?: break
                pendingDiffBase = null
                _state.update { it.copy(isDiffLoading = true, diffError = null) }
                try {
                    val diff = git.diffAgainst(baseRef)
                    if (repository !== git) return@launch
                    _state.update { it.copy(diff = diff, isDiffLoading = false) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (repository !== git) return@launch
                    _state.update {
                        it.copy(diff = emptyList(), isDiffLoading = false, diffError = e.message ?: e.toString())
                    }
                }
            }
        }
    }

    /** Runs the review agent against the current branch and base ref. */
    fun reviewBranch() {
        val git = repository ?: return
        if (_state.value.isReviewing) return
        _state.update { it.copy(isReviewing = true, reviewError = null, reviewToolUses = emptyList()) }
        viewModelScope.launch {
            try {
                // The agent's tool loop is sequential, so a plain list needs no locking.
                val toolUses = mutableListOf<ReviewToolUse>()
                val result = withTimeout(REVIEW_TIMEOUT) { reviewAgent.review(git, _state.value.baseRef, toolUses::add) }
                if (repository !== git) return@launch
                _state.update { it.copy(review = result, reviewToolUses = toolUses.toList(), isReviewing = false, sharedReviewId = null, shareFailure = null, openedSharedReview = null) }
            } catch (e: TimeoutCancellationException) {
                // A CancellationException subclass: it must be caught before the generic one.
                if (repository === git) _state.update { it.copy(isReviewing = false, reviewError = ReviewFailure.Timeout) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(isReviewing = false, reviewError = e.toReviewFailure()) }
            }
        }
    }

    fun applySuggestion(comment: ReviewComment) {
        val git = repository ?: return
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            try {
                writeLock.withLock {
                    val content = git.readFile(comment.filePath)
                        ?: error("File not found: ${comment.filePath}")
                    git.writeFile(comment.filePath, ReviewAnchoring.apply(content, comment))
                }
                // The user may have opened another repository while the write ran.
                if (repository !== git) return@launch
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
                if (repository === git) _state.update { it.copy(actionError = e.message ?: e.toString()) }
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
        _state.update { it.copy(actionError = null) }
        if (_state.value.appliedFiles.isEmpty()) return
        viewModelScope.launch {
            try {
                // Read the applied files inside the lock: an apply still writing must be included.
                writeLock.withLock {
                    val state = _state.value
                    git.stage(state.appliedFiles.toList())
                    git.commit(state.commitMessage)
                }
                if (repository !== git) return@launch
                val commits = git.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                // The nits not applied yet stay, moved to their new lines: they can still be shared or applied elsewhere.
                val committed = _state.value
                val remaining = reanchor(git, committed.review?.comments.orEmpty().filterNot { it.id in committed.appliedIds })
                _state.update {
                    it.copy(
                        graph = graph,
                        refs = git.refs(),
                        tracking = git.trackingStatus(),
                        selectedCommitId = graph.firstOrNull()?.commit?.id,
                        review = it.review?.takeIf { remaining.isNotEmpty() }?.copy(comments = remaining),
                        reviewError = null,
                        sharedReviewId = null,
                        shareFailure = null,
                        openedSharedReview = null,
                        appliedIds = emptySet(),
                        appliedFiles = emptySet(),
                        commitMessage = "",
                        lastPull = null,
                    )
                }
                refreshDiff()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(actionError = e.message ?: e.toString()) }
            }
        }
    }

    /** Sends the review on screen to the sharing server, under the repository's origin URL. */
    fun shareReview() {
        val git = repository ?: return
        val client = shareClient ?: return
        val state = _state.value
        val review = state.review ?: return
        if (state.isSharing) return
        _state.update { it.copy(isSharing = true, shareFailure = null) }
        viewModelScope.launch {
            val outcome = shareCall(git) { repoUrl ->
                val head = state.refs.firstOrNull { it.type == RefType.LOCAL_BRANCH && it.name == state.currentBranch }?.commitId
                client.share(
                    SharedReview(
                        repoUrl = repoUrl,
                        branch = state.currentBranch,
                        baseRef = state.baseRef,
                        // A detached HEAD reports the commit id as its branch name.
                        headCommit = head ?: state.currentBranch,
                        result = review,
                    ),
                )
            } ?: return@launch
            _state.update {
                outcome.fold(
                    { id -> it.copy(isSharing = false, sharedReviewId = id) },
                    { failure -> it.copy(isSharing = false, shareFailure = failure) },
                )
            }
        }
    }

    /** Lists the reviews shared for this repository, newest first. */
    fun loadSharedReviews() {
        val git = repository ?: return
        val client = shareClient ?: return
        sharedLoading?.cancel()
        _state.update { it.copy(isLoadingSharedReviews = true, sharedReviewsFailure = null) }
        sharedLoading = viewModelScope.launch {
            val outcome = shareCall(git) { repoUrl -> client.list(repoUrl) } ?: return@launch
            _state.update {
                outcome.fold(
                    { reviews -> it.copy(isLoadingSharedReviews = false, sharedReviews = reviews) },
                    { failure -> it.copy(isLoadingSharedReviews = false, sharedReviewsFailure = failure) },
                )
            }
        }
    }

    /**
     * Shows a shared review like a local one, without calling the LLM. It first switches to the
     * review's branch, whose files the nits point at, then re-anchors every nit on the files here:
     * a commit made since moves them, or sends them to the whole file.
     */
    fun openSharedReview(shared: SharedReview) {
        val git = repository ?: return
        if (_state.value.isSyncing || _state.value.isReviewing) return
        viewModelScope.launch {
            // A failed checkout shows its error and the nits land on the current files instead.
            if (shared.branch != _state.value.currentBranch) switchTo(git, shared.branch)
            if (repository !== git) return@launch
            val comments = try {
                reanchor(git, shared.result.comments)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(actionError = e.message ?: e.toString()) }
                return@launch
            }
            if (repository !== git) return@launch
            _state.update {
                it.copy(
                    review = shared.result.copy(comments = comments),
                    reviewError = null,
                    // A shared review ran its tools on another device: there is nothing to show here.
                    reviewToolUses = emptyList(),
                    sharedReviewId = shared.id,
                    shareFailure = null,
                    openedSharedReview = shared,
                )
            }
        }
    }

    /**
     * Runs [block] with the origin URL, token stripped. Returns null when another repository was
     * opened meanwhile, so a late answer never lands on it.
     */
    private suspend fun <T> shareCall(git: GitRepository, block: suspend (repoUrl: String) -> T): ShareOutcome<T>? {
        val outcome = try {
            val repoUrl = git.remoteUrl()?.let(::withoutCredentials)
            if (repoUrl == null) ShareOutcome.Failed(ShareFailure.NoRemote) else ShareOutcome.Done(block(repoUrl))
        } catch (e: CancellationException) {
            throw e
        } catch (e: ReviewShareException) {
            ShareOutcome.Failed(ShareFailure.Unreachable)
        }
        return outcome.takeIf { repository === git }
    }

    /** Updates the remote refs and the ahead/behind counters; the working tree stays untouched. */
    fun fetchBranch(credentials: Credentials?) {
        val git = repository ?: return
        if (_state.value.isSyncing) return
        _state.update { it.copy(isFetching = true, fetchError = null) }
        viewModelScope.launch {
            try {
                git.fetch(credentials)
                if (repository !== git) return@launch
                _state.update {
                    it.copy(
                        isFetching = false,
                        refs = git.refs(),
                        tracking = git.trackingStatus(),
                        lastPull = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(isFetching = false, fetchError = e.message ?: e.toString()) }
            }
        }
    }

    /** Pushes the current branch to origin with the forge connection from the settings. */
    fun pushBranch(credentials: Credentials?) {
        val git = repository ?: return
        if (_state.value.isSyncing) return
        _state.update { it.copy(isPushing = true, pushError = null) }
        viewModelScope.launch {
            try {
                git.push(credentials)
                if (repository !== git) return@launch
                _state.update { it.copy(isPushing = false, refs = git.refs(), tracking = git.trackingStatus(), lastPull = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(isPushing = false, pushError = e.message ?: e.toString()) }
            }
        }
    }

    /** Fetches and fast-forwards the current branch from its upstream; a conflict is reported, never merged. */
    fun pullBranch(credentials: Credentials?) {
        val git = repository ?: return
        // Applied but uncommitted suggestions would be merged over; commit them first.
        if (_state.value.isSyncing || _state.value.isReviewing || _state.value.appliedFiles.isNotEmpty()) return
        _state.update { it.copy(isPulling = true, pullError = null, fetchError = null, lastPull = null) }
        viewModelScope.launch {
            try {
                val result = git.pull(credentials)
                val commits = git.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                if (repository !== git) return@launch
                _state.update {
                    if (result.updated) {
                        // New commits can move the lines the nits point at: the review restarts from scratch.
                        it.copy(
                            isPulling = false,
                            lastPull = result,
                            graph = graph,
                            refs = git.refs(),
                            tracking = git.trackingStatus(),
                            selectedCommitId = graph.firstOrNull()?.commit?.id,
                            review = null,
                            reviewError = null,
                            reviewToolUses = emptyList(),
                            sharedReviewId = null,
                            shareFailure = null,
                            openedSharedReview = null,
                            appliedIds = emptySet(),
                            commitMessage = "",
                        )
                    } else {
                        it.copy(isPulling = false, lastPull = result, refs = git.refs(), tracking = git.trackingStatus())
                    }
                }
                if (result.updated) refreshDiff()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(isPulling = false, pullError = e.message ?: e.toString()) }
            }
        }
    }

    /** Switches to [branch] and reloads the graph, the diff and the review for it. */
    fun checkoutBranch(branch: String) {
        val git = repository ?: return
        // A review still running would land its nits on the new branch.
        if (_state.value.isSyncing || _state.value.isReviewing || branch == _state.value.currentBranch) return
        viewModelScope.launch { switchTo(git, branch) }
    }

    /** Checks out [branch] and reloads graph and diff; false when the checkout failed or another repository was opened. */
    private suspend fun switchTo(git: GitRepository, branch: String): Boolean {
        _state.update { it.copy(isCheckingOut = true, checkoutError = null) }
        try {
            git.checkout(branch)
            val commits = git.log()
            val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
            if (repository !== git) return false
            _state.update {
                it.copy(
                    isCheckingOut = false,
                    currentBranch = git.currentBranch(),
                    graph = graph,
                    refs = git.refs(),
                    tracking = git.trackingStatus(),
                    selectedCommitId = graph.firstOrNull()?.commit?.id,
                    review = null,
                    reviewError = null,
                    reviewToolUses = emptyList(),
                    sharedReviewId = null,
                    shareFailure = null,
                    openedSharedReview = null,
                    appliedIds = emptySet(),
                    appliedFiles = emptySet(),
                    commitMessage = "",
                    pushError = null,
                    pullError = null,
                    lastPull = null,
                )
            }
            refreshDiff()
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (repository === git) _state.update { it.copy(isCheckingOut = false, checkoutError = e.message ?: e.toString()) }
            return false
        }
    }

    /** Moves each nit to where its snippet is in the files on disk now, or to the whole file. */
    private suspend fun reanchor(git: GitRepository, comments: List<ReviewComment>): List<ReviewComment> {
        // Several nits often share a file: read it once instead of once per nit.
        val contents = HashMap<String, String?>()
        return comments.map { comment ->
            if (!contents.containsKey(comment.filePath)) contents[comment.filePath] = git.readFile(comment.filePath)
            ReviewAnchoring.anchorOrFileLevel(contents[comment.filePath], comment)
        }
    }
}

private sealed interface ShareOutcome<out T> {
    data class Done<T>(val value: T) : ShareOutcome<T>
    data class Failed(val failure: ShareFailure) : ShareOutcome<Nothing>

    fun <R> fold(onDone: (T) -> R, onFailed: (ShareFailure) -> R): R = when (this) {
        is Done -> onDone(value)
        is Failed -> onFailed(failure)
    }
}

private val DEFAULT_BASE_CANDIDATES = listOf("main", "origin/main", "master", "origin/master")

/** The branch a review compares against: a repository cloned from GitHub often has no local `main`. */
internal fun defaultBaseRef(refs: List<Ref>): String {
    val names = refs.filter { it.type != RefType.TAG }.map { it.name }.toSet()
    return DEFAULT_BASE_CANDIDATES.firstOrNull { it in names } ?: DEFAULT_BASE_CANDIDATES.first()
}
