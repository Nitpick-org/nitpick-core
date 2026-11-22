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
import dev.nitpick.app.review.JunieUnavailableException
import dev.nitpick.app.review.MissingApiKeyException
import dev.nitpick.app.review.ReviewAgent
import dev.nitpick.app.review.ReviewAnchoring
import dev.nitpick.app.review.isReviewablePath
import dev.nitpick.app.review.ReviewProviderException
import dev.nitpick.app.review.ReviewResponseException
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
    /** The Junie CLI is missing or signed out: a terminal fixes it, not the settings. */
    data object JunieUnavailable : ReviewFailure
    data object Network : ReviewFailure
    data object Timeout : ReviewFailure
    data object InvalidAnswer : ReviewFailure
    data class Other(val message: String) : ReviewFailure
}

sealed interface ShareFailure {
    /** No `origin` remote: nothing identifies the repository on another device. */
    data object NoRemote : ShareFailure
    data object Unreachable : ShareFailure
    /** The server answered, but refused: wrong token, body too large, versions that disagree. */
    data class Rejected(val status: Int) : ShareFailure
}

private fun Throwable.toReviewFailure(): ReviewFailure = when {
    this is MissingApiKeyException -> ReviewFailure.MissingKey(provider)
    this is JunieUnavailableException -> ReviewFailure.JunieUnavailable
    isTimeout() -> ReviewFailure.Timeout
    this is ReviewProviderException -> ReviewFailure.Network
    this is ReviewResponseException -> ReviewFailure.InvalidAnswer
    else -> ReviewFailure.Other(message ?: toString())
}

private fun Throwable.isTimeout(): Boolean {
    val causes = generateSequence(this as Throwable?) { it.cause }.toList()
    if (causes.any { it is TimeoutCancellationException || it is io.ktor.client.plugins.HttpRequestTimeoutException }) {
        return true
    }
    // Koog wraps provider timeouts in ReviewProviderException/LLMClientException without the
    // Ktor type; keep a narrow message fallback only for provider errors (#146).
    return this is ReviewProviderException && causes.any { cause ->
        cause.message?.lowercase()?.let { "timeout" in it || "timed out" in it } == true
    }
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
    // The branch the last push sent, until the next commit or sync; drives the « Pushed » feedback.
    val lastPushedBranch: String? = null,
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

    /**
     * A pull or a checkout is writing files: a suggestion applied meanwhile could be erased by the
     * reset that undoes a conflicting pull. A fetch or a push leaves the files alone.
     */
    val isRewritingWorkingTree: Boolean get() = isPulling || isCheckingOut

    /** Local commits the origin does not have yet: true when the current branch is ahead of, or unknown to, origin. */
    val hasUnpushedCommits: Boolean
        get() {
            tracking?.let { return it.ahead > 0 }
            val local = refs.firstOrNull { it.type == RefType.LOCAL_BRANCH && it.name == currentBranch } ?: return false
            val remote = refs.firstOrNull { it.type == RefType.REMOTE_BRANCH && it.name == "origin/$currentBranch" }
            return remote?.commitId != local.commitId
        }

    /** A pull would merge over the applied suggestions, or land under a running review: it waits for the commit. */
    val isPullBlocked: Boolean get() = appliedFiles.isNotEmpty() || isReviewing

    /** A remote named origin, even when the current branch has no upstream yet. */
    val hasOrigin: Boolean
        get() = refs.any { it.type == RefType.REMOTE_BRANCH && it.name.startsWith("origin/") }

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
            } catch (e: Throwable) {
                // Also an Error (a missing JDK module, #245): a message beats an endless loader.
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
        _state.update { it.copy(isReviewing = true, reviewError = null, actionError = null) }
        viewModelScope.launch {
            try {
                val result = withTimeout(REVIEW_TIMEOUT) { reviewAgent.review(git, _state.value.baseRef) }
                if (repository !== git) return@launch
                // A new review reuses file:line ids: keep appliedFiles for the commit, but the new
                // nits must not inherit the previous "Applied" marks (#137).
                _state.update {
                    it.copy(
                        review = result,
                        isReviewing = false,
                        appliedIds = emptySet(),
                        sharedReviewId = null,
                        shareFailure = null,
                        openedSharedReview = null,
                    )
                }
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
        if (_state.value.isRewritingWorkingTree) return
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            try {
                writeLock.withLock {
                    // A double tap must not run the same patch twice, and a nit already committed
                    // (so no longer in the review) must not be applied again from a stale click.
                    val current = _state.value
                    if (comment.id in current.appliedIds || current.review?.comments?.none { it.id == comment.id } != false) return@withLock
                    val content = git.readFile(comment.filePath)
                        ?: error("File not found: ${comment.filePath}")
                    val updated = ReviewAnchoring.apply(content, comment)
                    git.writeFile(comment.filePath, updated)
                    if (repository !== git) return@withLock
                    _state.update {
                        // A patch can add lines above another nit: move the remaining nits of the
                        // same file before the next Apply writes at a stale line (#156).
                        val comments = it.review?.comments.orEmpty().map { other ->
                            if (other.id == comment.id || other.filePath != comment.filePath) {
                                other
                            } else {
                                ReviewAnchoring.anchorOrFileLevel(updated, other)
                            }
                        }
                        val appliedBefore = it.review?.comments.orEmpty().filter { nit -> nit.id in it.appliedIds }
                        // The user's own message stays; the proposed one follows what is applied.
                        val untouched = it.commitMessage.isBlank() || it.commitMessage == commitMessageFor(appliedBefore)
                        it.copy(
                            review = it.review?.copy(comments = comments),
                            appliedIds = it.appliedIds + comment.id,
                            appliedFiles = it.appliedFiles + comment.filePath,
                            commitMessage = if (untouched) commitMessageFor(appliedBefore + comment) else it.commitMessage,
                        )
                    }
                }
                if (repository === git) refreshDiff()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(actionError = e.message ?: e.toString()) }
            }
        }
    }

    /** Applies every remaining suggestion of [filePath], bottom-up so earlier lines do not shift. */
    fun applyAllSuggestions(filePath: String) {
        val git = repository ?: return
        if (_state.value.isRewritingWorkingTree) return
        _state.update { it.copy(actionError = null) }
        viewModelScope.launch {
            try {
                writeLock.withLock {
                    val state = _state.value
                    val review = state.review ?: return@withLock
                    val targets = review.comments
                        .filter { it.filePath == filePath && !it.isFileLevel && it.suggestion != null && it.id !in state.appliedIds }
                        .sortedByDescending { it.startLine }
                    if (targets.isEmpty()) return@withLock
                    var content = git.readFile(filePath) ?: error("File not found: $filePath")
                    val applied = state.appliedIds.toMutableSet()
                    targets.forEach { comment ->
                        content = ReviewAnchoring.apply(content, ReviewAnchoring.anchorOrFileLevel(content, comment))
                        applied += comment.id
                    }
                    git.writeFile(filePath, content)
                    if (repository !== git) return@withLock
                    _state.update {
                        val comments = it.review?.comments.orEmpty().map { other ->
                            if (other.filePath == filePath && other.id !in applied) {
                                ReviewAnchoring.anchorOrFileLevel(content, other)
                            } else {
                                other
                            }
                        }
                        val appliedComments = it.review?.comments.orEmpty().filter { comment -> comment.id in applied }
                        val appliedBefore = it.review?.comments.orEmpty().filter { comment -> comment.id in it.appliedIds }
                        val untouched = it.commitMessage.isBlank() || it.commitMessage == commitMessageFor(appliedBefore)
                        it.copy(
                            review = it.review?.copy(comments = comments),
                            appliedIds = applied,
                            appliedFiles = it.appliedFiles + filePath,
                            commitMessage = if (untouched) commitMessageFor(appliedComments) else it.commitMessage,
                        )
                    }
                }
                if (repository === git) refreshDiff()
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
        if (_state.value.isRewritingWorkingTree) return
        _state.update { it.copy(actionError = null) }
        if (_state.value.appliedFiles.isEmpty()) return
        viewModelScope.launch {
            try {
                // Hold the lock until appliedFiles is cleared: a second tap must see the commit done.
                writeLock.withLock {
                    val state = _state.value
                    if (state.appliedFiles.isEmpty()) return@withLock
                    git.stage(state.appliedFiles.toList())
                    git.commit(state.commitMessage, state.appliedFiles.toList())
                    if (repository !== git) return@withLock
                    val commits = git.log()
                    val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                    val refs = git.refs()
                    val tracking = git.trackingStatus()
                    // The nits not applied yet stay, moved to their new lines.
                    val remaining = reanchor(git, state.review?.comments.orEmpty().filterNot { it.id in state.appliedIds })
                    _state.update {
                        it.copy(
                            graph = graph,
                            refs = refs,
                            tracking = tracking,
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
                            lastPushedBranch = null,
                        )
                    }
                }
                if (repository === git) refreshDiff()
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
                    // A review run again while the share was in flight was never sent: no badge on it.
                    { id -> if (it.review === review) it.copy(isSharing = false, sharedReviewId = id) else it.copy(isSharing = false) },
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
        // Switching branch would carry applied edits to another branch and lose the commit bar (#138).
        if (_state.value.isSyncing || _state.value.isReviewing || _state.value.appliedFiles.isNotEmpty()) return
        viewModelScope.launch {
            // A failed checkout shows its error and the nits land on the current files instead.
            if (shared.branch != _state.value.currentBranch) switchTo(git, shared.branch)
            if (repository !== git) return@launch
            val comments = try {
                // A shared review comes from the network: keep its nits on tracked, reviewable
                // files only, like a local review (no secrets file, no nested .git, no untracked file).
                val tracked = git.listFiles().toHashSet()
                reanchor(git, shared.result.comments.filter { it.filePath in tracked && isReviewablePath(it.filePath) })
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
                    actionError = null,
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
            ShareOutcome.Failed(e.status?.let { ShareFailure.Rejected(it) } ?: ShareFailure.Unreachable)
        }
        return outcome.takeIf { repository === git }
    }

    /** Updates the remote refs and the ahead/behind counters; the working tree stays untouched. */
    fun fetchBranch(credentials: Credentials?) {
        val git = repository ?: return
        if (_state.value.isSyncing) return
        _state.update { it.copy(isFetching = true, fetchError = null, pushError = null) }
        viewModelScope.launch {
            try {
                git.fetch(credentials)
                val refs = git.refs()
                val tracking = git.trackingStatus()
                if (repository !== git) return@launch
                _state.update {
                    it.copy(
                        isFetching = false,
                        refs = refs,
                        tracking = tracking,
                        lastPull = null,
                        lastPushedBranch = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(isFetching = false, fetchError = e.message ?: e.toString()) }
            }
        }
    }

    /** Publishes a local branch with no upstream to origin and records the upstream. */
    fun publishBranch(credentials: Credentials?) {
        val git = repository ?: return
        if (_state.value.isSyncing) return
        _state.update { it.copy(isPushing = true, pushError = null, lastPushedBranch = null) }
        viewModelScope.launch {
            try {
                git.publishBranch(credentials)
                val refs = git.refs()
                val tracking = git.trackingStatus()
                if (repository !== git) return@launch
                _state.update {
                    it.copy(
                        isPushing = false,
                        refs = refs,
                        tracking = tracking,
                        lastPull = null,
                        lastPushedBranch = it.currentBranch,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (repository === git) _state.update { it.copy(isPushing = false, pushError = e.message ?: e.toString()) }
            }
        }
    }

    /** Pushes the current branch to origin with the forge connection from the settings. */
    fun pushBranch(credentials: Credentials?) {
        val git = repository ?: return
        if (_state.value.isSyncing) return
        _state.update { it.copy(isPushing = true, pushError = null, lastPushedBranch = null) }
        viewModelScope.launch {
            try {
                git.push(credentials)
                val refs = git.refs()
                val tracking = git.trackingStatus()
                if (repository !== git) return@launch
                _state.update { it.copy(isPushing = false, refs = refs, tracking = tracking, lastPull = null, lastPushedBranch = it.currentBranch) }
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
        if (_state.value.isSyncing || _state.value.isPullBlocked) return
        _state.update { it.copy(isPulling = true, pullError = null, fetchError = null, pushError = null, lastPull = null, lastPushedBranch = null) }
        viewModelScope.launch {
            try {
                val result = git.pull(credentials)
                val commits = git.log()
                val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
                val refs = git.refs()
                val tracking = git.trackingStatus()
                if (repository !== git) return@launch
                _state.update {
                    if (result.updated) {
                        // New commits can move the lines the nits point at: the review restarts from scratch.
                        it.copy(
                            isPulling = false,
                            lastPull = result,
                            lastPushedBranch = null,
                            graph = graph,
                            refs = refs,
                            tracking = tracking,
                            selectedCommitId = graph.firstOrNull()?.commit?.id,
                            review = null,
                            reviewError = null,
                            actionError = null,
                            sharedReviewId = null,
                            shareFailure = null,
                            openedSharedReview = null,
                            appliedIds = emptySet(),
                            commitMessage = "",
                        )
                    } else {
                        it.copy(isPulling = false, lastPull = result, lastPushedBranch = null, refs = refs, tracking = tracking)
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
        // A review still running would land its nits on the new branch; applied edits must be
        // committed or discarded before leaving this branch (#138).
        if (_state.value.isSyncing || _state.value.isReviewing || _state.value.appliedFiles.isNotEmpty() || branch == _state.value.currentBranch) return
        viewModelScope.launch { switchTo(git, branch) }
    }

    /** Checks out [branch] and reloads graph and diff; false when the checkout failed or another repository was opened. */
    private suspend fun switchTo(git: GitRepository, branch: String): Boolean {
        _state.update { it.copy(isCheckingOut = true, checkoutError = null, actionError = null) }
        try {
            git.checkout(branch)
            val commits = git.log()
            val graph = withContext(graphDispatcher) { buildCommitGraph(commits) }
            val currentBranch = git.currentBranch()
            val refs = git.refs()
            val tracking = git.trackingStatus()
            if (repository !== git) return false
            _state.update {
                it.copy(
                    isCheckingOut = false,
                    currentBranch = currentBranch,
                    graph = graph,
                    refs = refs,
                    tracking = tracking,
                    selectedCommitId = graph.firstOrNull()?.commit?.id,
                    review = null,
                    reviewError = null,
                    sharedReviewId = null,
                    shareFailure = null,
                    openedSharedReview = null,
                    appliedIds = emptySet(),
                    appliedFiles = emptySet(),
                    commitMessage = "",
                    pushError = null,
                    pullError = null,
                    lastPull = null,
                        lastPushedBranch = null,
                )
            }
            refreshDiff()
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // JGit puts the conflicting paths after a newline; the status bar is one line.
            val message = (e.message ?: e.toString()).lineSequence()
                .joinToString(" ") { it.trim() }
                .trim()
            if (repository === git) _state.update { it.copy(isCheckingOut = false, checkoutError = message) }
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
