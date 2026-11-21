package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.git.FakeGitRepository
import dev.nitpick.app.git.FakeGitRepositoryFactory
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

    private val repositories = FakeGitRepositoryFactory(
        mapOf("/work/demo" to FakeGitRepository(branch = "main", commits = commits, refs = listOf(mainBranch))),
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
            ),
            viewModel.state.value,
        )
        assertEquals(child, viewModel.state.value.selectedCommit)
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

    private fun commit(id: String, message: String, parents: List<String>) = Commit(
        id = id,
        shortMessage = message,
        fullMessage = "$message\n",
        authorName = "Ada Lovelace",
        authorEmail = "ada@nitpick.dev",
        timestampEpochSeconds = 1_700_000_000,
        parentIds = parents,
    )
}
