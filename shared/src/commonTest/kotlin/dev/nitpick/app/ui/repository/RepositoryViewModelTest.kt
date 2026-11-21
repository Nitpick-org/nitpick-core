package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
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
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.graph.buildCommitGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun aFailedCloneShowsTheErrorOnTheTargetPath() {
        val viewModel = RepositoryViewModel(RecordingCloneFactory(cloned = null), graphDispatcher = dispatcher)

        viewModel.cloneRepository("https://github.com/acme/demo.git", "/data/repos/demo", credentials = null)

        assertEquals(
            RepositoryUiState(path = "/data/repos/demo", errorMessage = "not authorized"),
            viewModel.state.value,
        )
    }

    /** Records the last clone request; a null [cloned] repository makes the clone fail like a bad token. */
    private class RecordingCloneFactory(private val cloned: GitRepository?) : GitRepositoryFactory {
        var lastClone: Triple<String, String, Credentials?>? = null

        override suspend fun open(path: String): GitRepository = throw IllegalArgumentException("Not a git repository: $path")

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
        ReviewFailure.MissingKey("ANTHROPIC"),
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
        assertEquals(listOf("Apply review suggestions"), repo.commitMessages)
        assertEquals(null, viewModel.state.value.review)
        assertEquals(emptySet(), viewModel.state.value.appliedIds)
    }
}
