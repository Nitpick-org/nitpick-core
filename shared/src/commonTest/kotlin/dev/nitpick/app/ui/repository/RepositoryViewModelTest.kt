package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.git.FakeGitRepository
import dev.nitpick.app.git.FakeGitRepositoryFactory
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

    private val commit = Commit(
        id = "a1b2c3d4e5",
        shortMessage = "Add the graph",
        fullMessage = "Add the graph\n",
        authorName = "Ada Lovelace",
        authorEmail = "ada@nitpick.dev",
        timestampEpochSeconds = 1_700_000_000,
        parentIds = emptyList(),
    )
    private val mainBranch = Ref("main", "refs/heads/main", commit.id, RefType.LOCAL_BRANCH)

    // viewModelScope runs on Dispatchers.Main, which tests have to provide.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun startsWithNoRepositoryOpen() {
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(emptyMap()))

        assertEquals(RepositoryUiState(), viewModel.state.value)
    }

    @Test
    fun openingARepositoryShowsItsBranchCommitsAndRefs() {
        val repository = FakeGitRepository(branch = "main", commits = listOf(commit), refs = listOf(mainBranch))
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(mapOf("/work/demo" to repository)))

        viewModel.openRepository("/work/demo")

        assertEquals(
            RepositoryUiState(
                path = "/work/demo",
                currentBranch = "main",
                commits = listOf(commit),
                refs = listOf(mainBranch),
            ),
            viewModel.state.value,
        )
    }

    @Test
    fun openingAFolderThatIsNotARepositoryShowsTheError() {
        val viewModel = RepositoryViewModel(FakeGitRepositoryFactory(emptyMap()))

        viewModel.openRepository("/tmp/photos")

        assertEquals(
            RepositoryUiState(path = "/tmp/photos", errorMessage = "Not a git repository: /tmp/photos"),
            viewModel.state.value,
        )
    }
}
