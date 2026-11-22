package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.git.FakeGitRepository
import dev.nitpick.app.git.FakeGitRepositoryFactory
import dev.nitpick.app.git.GitRepository
import dev.nitpick.app.git.GitRepositoryFactory
import dev.nitpick.app.review.MissingApiKeyException
import dev.nitpick.app.review.ReviewAgent
import dev.nitpick.app.review.ReviewProviderException
import dev.nitpick.app.review.ReviewResponseException
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.graph.buildCommitGraph
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val parent = commit(id = "1111111aaaa", message = "Add the parser", parents = emptyList())
    private val child = commit(id = "2222222bbbb", message = "Add the graph", parents = listOf(parent.id))
    private val commits = listOf(child, parent)
    private val mainBranch = Ref("main", "refs/heads/main", child.id, RefType.LOCAL_BRANCH)

    private val parserChange = fileDiff("diff/UnifiedDiffParser.kt")
    private val graphChange = fileDiff("graph/CommitGraph.kt")
    private val releaseChange = fileDiff("README.md")
    private val diffs = mapOf("main" to listOf(parserChange, graphChange), "release" to listOf(releaseChange))

    private val repositories = FakeGitRepositoryFactory(
        mapOf(
            "/work/demo" to FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs),
        ),
    )

    // viewModelScope runs on Dispatchers.Main, which tests have to provide.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = RepositoryViewModel(repositories, graphDispatcher = dispatcher)

    @Test
    fun startsWithNoRepositoryOpen() {
        assertEquals(RepositoryUiState(), viewModel().state.value)
    }

    @Test
    fun openingARepositoryShowsItsBranchGraphAndRefsWithTheNewestCommitSelected() {
        val viewModel = viewModel()

        viewModel.openRepository("/work/demo")

        assertEquals(
            RepositoryUiState(
                path = "/work/demo",
                currentBranch = "main",
                graph = buildCommitGraph(commits),
                refs = listOf(mainBranch),
                selectedCommitId = child.id,
                diff = listOf(parserChange, graphChange),
            ),
            viewModel.state.value,
        )
        assertEquals(child, viewModel.state.value.selectedCommit)
        assertEquals(parserChange, viewModel.state.value.selectedDiff)
    }

    @Test
    fun selectingAChangedFileShowsItsHunks() {
        val viewModel = viewModel()
        viewModel.openRepository("/work/demo")

        viewModel.selectDiffFile(graphChange.path)

        assertEquals(graphChange, viewModel.state.value.selectedDiff)
    }

    @Test
    fun choosingAnotherBaseReloadsTheDiff() {
        val viewModel = viewModel()
        viewModel.openRepository("/work/demo")

        viewModel.selectBase("release")

        assertEquals("release", viewModel.state.value.baseRef)
        assertEquals(listOf(releaseChange), viewModel.state.value.diff)
        assertEquals(releaseChange, viewModel.state.value.selectedDiff)
    }

    @Test
    fun anUnknownBaseShowsTheErrorAndKeepsTheGraph() {
        val viewModel = viewModel()
        viewModel.openRepository("/work/demo")

        viewModel.selectBase("develop")

        val state = viewModel.state.value
        assertEquals("Unknown base: develop", state.diffError)
        assertEquals(emptyList(), state.diff)
        assertEquals(buildCommitGraph(commits), state.graph)
    }

    @Test
    fun selectingACommitShowsItsDetails() {
        val viewModel = viewModel()
        viewModel.openRepository("/work/demo")

        viewModel.selectCommit(parent.id)

        assertEquals(parent, viewModel.state.value.selectedCommit)
    }

    @Test
    fun openingAFolderThatIsNotARepositoryShowsTheError() {
        val viewModel = viewModel()

        viewModel.openRepository("/tmp/photos")

        assertEquals(
            RepositoryUiState(path = "/tmp/photos", errorMessage = "Not a git repository: /tmp/photos"),
            viewModel.state.value,
        )
    }

    @Test
    fun cloningARepositoryOpensTheCloneAndHandsTheCredentialsToGit() {
        val factory = RecordingCloneFactory(FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch)))
        val viewModel = RepositoryViewModel(factory, graphDispatcher = dispatcher)
        val credentials = Credentials(username = "x-access-token", token = "ghp_secret")

        viewModel.cloneRepository("https://github.com/acme/demo.git", "/data/repos/demo", credentials)

        assertEquals(Triple("https://github.com/acme/demo.git", "/data/repos/demo", credentials), factory.lastClone)
        val state = viewModel.state.value
        assertEquals("/data/repos/demo", state.path)
        assertEquals(buildCommitGraph(commits), state.graph)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun aFailedCloneKeepsTheOpenRepositoryAndReportsTheError() {
        val open = FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch))
        val viewModel = RepositoryViewModel(RecordingCloneFactory(cloned = null, opened = open), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/nitpick")
        val before = viewModel.state.value

        viewModel.cloneRepository("https://github.com/acme/demo.git", "/data/repos/demo", credentials = null)

        assertEquals(before.copy(clone = CloneStatus("/data/repos/demo", errorMessage = "not authorized")), viewModel.state.value)
    }

    @Test
    fun pushSendsTheCredentialsAndRefreshesTheRemoteBranch() {
        val remoteBehind = Ref("origin/main", "refs/remotes/origin/main", parent.id, RefType.REMOTE_BRANCH)
        val remote = PushingRepository(FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch, remoteBehind)))
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to remote)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")
        assertTrue(viewModel.state.value.hasUnpushedCommits)
        val credentials = Credentials("x-access-token", "ghp_secret")

        viewModel.pushBranch(credentials)

        assertEquals(listOf<Credentials?>(credentials), remote.pushes)
        val state = viewModel.state.value
        assertEquals(false, state.isPushing)
        assertEquals(null, state.pushError)
        assertEquals(false, state.hasUnpushedCommits)
        assertEquals("main", state.lastPushedBranch)
    }

    @Test
    fun aRejectedPushShowsTheErrorAndKeepsTheReview() {
        val remote = PushingRepository(FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch)), failure = "main: REJECTED_NONFASTFORWARD")
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to remote)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")

        viewModel.pushBranch(credentials = null)

        assertEquals("main: REJECTED_NONFASTFORWARD", viewModel.state.value.pushError)
        assertEquals(false, viewModel.state.value.isPushing)
    }

    @Test
    fun checkingOutABranchReloadsTheGraphAndDropsTheReview() {
        val featureTip = commit(id = "3333333cccc", message = "On feature", parents = listOf(child.id))
        val feature = Ref("feature", "refs/heads/feature", featureTip.id, RefType.LOCAL_BRANCH)
        val remote = PushingRepository(
            FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch, feature), diffs = diffs),
            afterCheckout = FakeGitRepository(branch = "feature", commits = listOf(featureTip) + commits, refs = listOf(mainBranch, feature), diffs = diffs),
        )
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to remote)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")

        viewModel.checkoutBranch("feature")

        assertEquals(listOf("feature"), remote.checkouts)
        val state = viewModel.state.value
        assertEquals("feature", state.currentBranch)
        assertEquals(featureTip.id, state.selectedCommitId)
        assertEquals(null, state.review)
        assertEquals(false, state.isCheckingOut)
    }

    @Test
    fun theBaseFallsBackToTheOriginBranchWhenThereIsNoLocalMain() {
        val remoteMaster = Ref("origin/master", "refs/remotes/origin/master", parent.id, RefType.REMOTE_BRANCH)
        val feature = Ref("feature", "refs/heads/feature", child.id, RefType.LOCAL_BRANCH)
        val tag = Ref("main", "refs/tags/main", child.id, RefType.TAG)
        assertEquals("origin/master", defaultBaseRef(listOf(remoteMaster, feature, tag)))
        assertEquals("main", defaultBaseRef(listOf(mainBranch, remoteMaster)))
        assertEquals("main", defaultBaseRef(emptyList()))

        val repository = FakeGitRepository(branch = "feature", commits = commits, refs = listOf(feature, remoteMaster), diffs = mapOf("origin/master" to listOf(parserChange)))
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/clone" to repository)), graphDispatcher = dispatcher)

        viewModel.openRepository("/work/clone")

        assertEquals("origin/master", viewModel.state.value.baseRef)
        assertEquals(listOf(parserChange), viewModel.state.value.diff)
    }

    @Test
    fun pullIsBlockedAndSaysSoWhileSuggestionsAreApplied() {
        assertEquals(false, RepositoryUiState().isPullBlocked)
        assertEquals(true, RepositoryUiState(appliedFiles = setOf("a.kt")).isPullBlocked)
        assertEquals(true, RepositoryUiState(isReviewing = true).isPullBlocked)
    }

    @Test
    fun pullReloadsTheGraphWhenCommitsArrived() {
        val incoming = commit(id = "4444444dddd", message = "From origin", parents = listOf(child.id))
        val remote = PushingRepository(
            FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs),
            pullResult = PullResult(updated = true, conflictedPaths = emptyList()),
            afterPull = FakeGitRepository(branch = "main", commits = listOf(incoming) + commits, refs = listOf(mainBranch), diffs = diffs),
        )
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to remote)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")

        viewModel.pullBranch(credentials = null)

        val state = viewModel.state.value
        assertEquals(PullResult(updated = true, conflictedPaths = emptyList()), state.lastPull)
        assertEquals(incoming.id, state.selectedCommitId)
        assertEquals(false, state.isPulling)
    }

    @Test
    fun aConflictingPullKeepsTheGraphAndNamesTheFiles() {
        val remote = PushingRepository(
            FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs),
            pullResult = PullResult(updated = false, conflictedPaths = listOf("a.txt")),
        )
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to remote)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")

        viewModel.pullBranch(credentials = null)

        val state = viewModel.state.value
        assertEquals(listOf("a.txt"), state.lastPull?.conflictedPaths)
        assertEquals(child.id, state.selectedCommitId)
        assertEquals(null, state.pullError)
    }

    @Test
    fun aCheckoutClearsThePreviousPullError() {
        val featureTip = commit(id = "3333333cccc", message = "On feature", parents = listOf(child.id))
        val feature = Ref("feature", "refs/heads/feature", featureTip.id, RefType.LOCAL_BRANCH)
        val remote = PushingRepository(
            FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch, feature), diffs = diffs),
            afterCheckout = FakeGitRepository(branch = "feature", commits = listOf(featureTip) + commits, refs = listOf(mainBranch, feature), diffs = diffs),
        )
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to remote)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")
        viewModel.pullBranch(credentials = null)
        assertEquals("no upstream", viewModel.state.value.pullError)

        viewModel.checkoutBranch("feature")

        assertEquals(null, viewModel.state.value.pullError)
        assertEquals("feature", viewModel.state.value.currentBranch)
    }

    /** The shared fake does not push or check out; this wrapper records both and swaps the branch view on checkout. */
    private class PushingRepository(
        private var inner: GitRepository,
        private val failure: String? = null,
        private val afterCheckout: GitRepository? = null,
        private val pullResult: PullResult? = null,
        private val afterPull: GitRepository? = null,
    ) : GitRepository by inner {
        val pushes = mutableListOf<Credentials?>()
        val checkouts = mutableListOf<String>()

        override suspend fun push(credentials: Credentials?) {
            pushes += credentials
            if (failure != null) throw IllegalArgumentException(failure)
        }

        override suspend fun checkout(branch: String) {
            checkouts += branch
            inner = afterCheckout ?: inner
        }

        override suspend fun pull(credentials: Credentials?): PullResult {
            inner = afterPull ?: inner
            return pullResult ?: error("no upstream")
        }

        override suspend fun log(limit: Int) = inner.log(limit)
        override suspend fun refs() = if (pushes.isNotEmpty() && failure == null) pushedRefs() else inner.refs()
        override suspend fun currentBranch() = inner.currentBranch()
        override suspend fun diffAgainst(baseRef: String) = inner.diffAgainst(baseRef)

        // After a push, origin/<branch> points where the local branch points.
        private suspend fun pushedRefs(): List<Ref> {
            val local = inner.refs().first { it.type == RefType.LOCAL_BRANCH && it.name == inner.currentBranch() }
            return inner.refs().filterNot { it.name == "origin/${local.name}" } +
                Ref("origin/${local.name}", "refs/remotes/origin/${local.name}", local.commitId, RefType.REMOTE_BRANCH)
        }
    }

    /** Records the last clone request; a null [cloned] repository makes the clone fail like a bad token. */
    private class RecordingCloneFactory(private val cloned: GitRepository?, private val opened: GitRepository? = null) : GitRepositoryFactory {
        var lastClone: Triple<String, String, Credentials?>? = null

        override suspend fun open(path: String): GitRepository = opened ?: throw IllegalArgumentException("Not a git repository: $path")

        override suspend fun clone(url: String, path: String, credentials: Credentials?): GitRepository {
            lastClone = Triple(url, path, credentials)
            return cloned ?: throw IllegalStateException("not authorized")
        }
    }

    private fun commit(id: String, message: String, parents: List<String>) = Commit(
        id = id,
        shortMessage = message,
        fullMessage = "$message\n",
        authorName = "Ada Lovelace",
        authorEmail = "ada@nitpick.dev",
        timestampEpochSeconds = 1_700_000_000,
        parentIds = parents,
    )

    private fun fileDiff(path: String) = FileDiff(path, oldPath = null, FileChangeType.MODIFIED, isBinary = false, hunks = emptyList())

    @Test
    fun aMissingApiKeyIsShownAsAnActionableFailure() = runReviewFailure(
        MissingApiKeyException(LlmProvider.ANTHROPIC),
        ReviewFailure.MissingKey(LlmProvider.ANTHROPIC),
    )

    @Test
    fun aProviderFailureIsAShownAsNetwork() = runReviewFailure(
        ReviewProviderException("request failed", IllegalStateException("connection reset")),
        ReviewFailure.Network,
    )

    @Test
    fun aTimeoutIsKeptDistinctFromANetworkFailure() = runReviewFailure(
        ReviewProviderException("request failed", IllegalStateException("request timed out")),
        ReviewFailure.Timeout,
    )

    @Test
    fun anInvalidModelAnswerIsShownAsItsOwnFailureNotRawText() = runReviewFailure(
        ReviewResponseException("The model answer is not a valid review: Unexpected JSON token at offset 12"),
        ReviewFailure.InvalidAnswer,
    )

    private fun runReviewFailure(error: Exception, expected: ReviewFailure) {
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult = throw error
        }
        val viewModel = RepositoryViewModel(repositories, graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        assertEquals(expected, viewModel.state.value.reviewError)
    }

    @Test
    fun aCheckoutConflictShowsTheBlockingFileOnOneLine() {
        val repo = FakeGitRepository(
            branch = "main",
            commits = commits,
            refs = listOf(mainBranch, Ref("feature", "refs/heads/feature", parent.id, RefType.LOCAL_BRANCH)),
            diffs = diffs,
            checkoutError = IllegalStateException("Checkout conflict with files:\nsrc/Graph.kt"),
        )
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher)
        viewModel.openRepository("/work/demo")

        viewModel.checkoutBranch("feature")

        assertEquals("Checkout conflict with files: src/Graph.kt", viewModel.state.value.checkoutError)
    }

    @Test
    fun applyingTheSameNitTwiceDoesNotPatchTwice() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()

        viewModel.applySuggestion(nit)
        viewModel.applySuggestion(nit)

        assertEquals("fun a() {\n    val lane = 0\n}\n", repo.files["graph/CommitGraph.kt"])
        assertEquals(null, viewModel.state.value.actionError)
        assertEquals(setOf("n1"), viewModel.state.value.appliedIds)
    }

    @Test
    fun reRunningAReviewClearsTheAppliedMarksButKeepsTheFiles() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        viewModel.applySuggestion(nit)
        assertEquals(setOf("n1"), viewModel.state.value.appliedIds)

        viewModel.reviewBranch()

        assertEquals(emptySet(), viewModel.state.value.appliedIds)
        assertEquals(setOf("graph/CommitGraph.kt"), viewModel.state.value.appliedFiles)
    }

    @Test
    fun checkingOutAnotherBranchIsIgnoredWhileSuggestionsAreApplied() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits,
            refs = listOf(mainBranch, Ref("release", "refs/heads/release", parent.id, RefType.LOCAL_BRANCH)),
            diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        viewModel.applySuggestion(nit)

        viewModel.checkoutBranch("release")

        assertEquals("main", viewModel.state.value.currentBranch)
        assertEquals(setOf("graph/CommitGraph.kt"), viewModel.state.value.appliedFiles)
    }

    @Test
    fun openingASharedReviewIsIgnoredWhileSuggestionsAreApplied() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits,
            refs = listOf(mainBranch, Ref("release", "refs/heads/release", parent.id, RefType.LOCAL_BRANCH)),
            diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        viewModel.applySuggestion(nit)

        viewModel.openSharedReview(
            SharedReview(
                id = "s1", repoUrl = "https://example.com/repo", branch = "release", baseRef = "main",
                headCommit = parent.id, result = ReviewResult("shared", listOf(nit)),
            ),
        )

        assertEquals("main", viewModel.state.value.currentBranch)
        assertEquals(setOf("graph/CommitGraph.kt"), viewModel.state.value.appliedFiles)
        assertNull(viewModel.state.value.openedSharedReview)
    }

    @Test
    fun applyingANitReanchorsTheOtherNitsOfTheSameFile() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val x = 1\n}\n"),
        )
        val first = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong value", originalSnippet = "val x = 1",
            suggestion = "    val x = 2\n    val y = 3",
        )
        val second = ReviewComment(
            id = "n2", filePath = "graph/CommitGraph.kt", startLine = 3, endLine = 3, severity = Severity.INFO,
            category = "style", message = "closing brace", originalSnippet = "}", suggestion = "}",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("2 nits", listOf(first, second))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()

        viewModel.applySuggestion(first)

        val moved = viewModel.state.value.review?.comments?.first { it.id == "n2" }
        assertEquals(4, moved?.startLine)
    }

    @Test
    fun applyingAllSuggestionsOfAFileWritesThemBottomUp() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val x = 1\n    val y = 2\n}\n"),
        )
        val first = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong x", originalSnippet = "val x = 1", suggestion = "    val x = 10",
        )
        val second = ReviewComment(
            id = "n2", filePath = "graph/CommitGraph.kt", startLine = 3, endLine = 3, severity = Severity.BUG,
            category = "logic", message = "wrong y", originalSnippet = "val y = 2", suggestion = "    val y = 20",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("2 nits", listOf(first, second))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()

        viewModel.applyAllSuggestions("graph/CommitGraph.kt")

        assertEquals("fun a() {\n    val x = 10\n    val y = 20\n}\n", repo.files["graph/CommitGraph.kt"])
        assertEquals(setOf("n1", "n2"), viewModel.state.value.appliedIds)
    }

    @Test
    fun committingTwiceCreatesOneCommit() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        viewModel.applySuggestion(nit)

        viewModel.commitAppliedSuggestions()
        viewModel.commitAppliedSuggestions()

        assertEquals(listOf("fix: wrong lane"), repo.commitMessages)
        // Only the applied file goes into the commit: a file staged by hand stays staged.
        assertEquals(listOf(listOf("graph/CommitGraph.kt")), repo.committedPaths)
        assertEquals(null, viewModel.state.value.actionError)
    }

    @Test
    fun aStaleApplyClickAfterTheCommitIsIgnored() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        viewModel.applySuggestion(nit)
        viewModel.commitAppliedSuggestions()

        viewModel.applySuggestion(nit)

        assertEquals("fun a() {\n    val lane = 0\n}\n", repo.files["graph/CommitGraph.kt"])
        assertEquals(null, viewModel.state.value.actionError)
        assertEquals(1, repo.commitMessages.size)
    }

    @Test
    fun anApplyWhileAPullRunsIsIgnored() {
        // A pull that conflicts ends in a hard reset: a file patched during the fetch would be erased.
        val original = "fun a() {\n    val lane = 1\n}\n"
        val files = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to original),
        )
        val pullDone = CompletableDeferred<Unit>()
        val repo = object : GitRepository by files {
            override suspend fun pull(credentials: Credentials?): PullResult {
                pullDone.await()
                return PullResult(updated = false, conflictedPaths = emptyList())
            }
        }
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()

        viewModel.pullBranch(credentials = null)
        viewModel.applySuggestion(nit)
        pullDone.complete(Unit)

        assertEquals(original, files.files["graph/CommitGraph.kt"])
        assertEquals(emptySet(), viewModel.state.value.appliedIds)
    }

    @Test
    fun reviewApplyAndCommitWriteTheFileStageItAndResetTheReview() {
        val repo = FakeGitRepository(
            branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs,
            files = mapOf("graph/CommitGraph.kt" to "fun a() {\n    val lane = 1\n}\n"),
        )
        val nit = ReviewComment(
            id = "n1", filePath = "graph/CommitGraph.kt", startLine = 2, endLine = 2, severity = Severity.BUG,
            category = "logic", message = "wrong lane", originalSnippet = "val lane = 1", suggestion = "    val lane = 0",
        )
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")

        viewModel.reviewBranch()
        assertEquals(listOf(nit), viewModel.state.value.review?.comments)

        viewModel.applySuggestion(nit)
        assertEquals("fun a() {\n    val lane = 0\n}\n", repo.files["graph/CommitGraph.kt"])
        assertEquals(setOf("n1"), viewModel.state.value.appliedIds)

        viewModel.commitAppliedSuggestions()
        assertEquals(listOf("graph/CommitGraph.kt"), repo.staged)
        assertEquals(listOf("fix: wrong lane"), repo.commitMessages)
        assertEquals(null, viewModel.state.value.review)
        assertEquals(emptySet(), viewModel.state.value.appliedIds)
    }

    @Test
    fun aReviewThatNeverAnswersEndsAsATimeout() {
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult { delay(10.minutes); error("unreachable") }
        }
        val viewModel = RepositoryViewModel(repositories, graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        assertNull(viewModel.state.value.reviewError)

        dispatcher.scheduler.advanceTimeBy(REVIEW_TIMEOUT.inWholeMilliseconds + 1)
        dispatcher.scheduler.runCurrent()

        assertEquals(ReviewFailure.Timeout, viewModel.state.value.reviewError)
    }

    @Test
    fun aFailedApplyKeepsTheReviewAndReportsTheActionError() {
        val repo = FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs, files = mapOf("graph/CommitGraph.kt" to "nothing here\n"))
        val nit = ReviewComment("n1", "graph/CommitGraph.kt", 1, 1, Severity.BUG, "logic", "m", originalSnippet = "val lane = 1", suggestion = "val lane = 0")
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()

        viewModel.applySuggestion(nit)

        assertNotNull(viewModel.state.value.actionError)
        assertNull(viewModel.state.value.reviewError)
        assertEquals(listOf(nit), viewModel.state.value.review?.comments)
    }

    @Test
    fun aPullThatDropsTheReviewAlsoDropsItsActionError() {
        val files = FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch), diffs = diffs, files = mapOf("graph/CommitGraph.kt" to "nothing here\n"))
        val repo = PushingRepository(files, pullResult = PullResult(updated = true, conflictedPaths = emptyList()))
        val nit = ReviewComment("n1", "graph/CommitGraph.kt", 1, 1, Severity.BUG, "logic", "m", originalSnippet = "val lane = 1", suggestion = "val lane = 0")
        val agent = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("1 bug", listOf(nit))
        }
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repo)), graphDispatcher = dispatcher, reviewAgent = agent)
        viewModel.openRepository("/work/demo")
        viewModel.reviewBranch()
        viewModel.applySuggestion(nit)
        assertNotNull(viewModel.state.value.actionError)

        viewModel.pullBranch(credentials = null)

        assertNull(viewModel.state.value.review)
        assertNull(viewModel.state.value.actionError)
    }
}
