package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.ReviewComment
import dev.nitpick.app.domain.ReviewResult
import dev.nitpick.app.domain.Severity
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.git.FakeGitRepository
import dev.nitpick.app.git.FakeGitRepositoryFactory
import dev.nitpick.app.git.GitRepository
import dev.nitpick.app.review.ReviewAgent
import dev.nitpick.app.share.ReviewShareClient
import dev.nitpick.app.share.ReviewShareException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ShareReviewViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val head = Commit("cfd2c60aaaa", "Paginate", "Paginate\n", "Ada", "ada@nitpick.dev", 1_700_000_000, emptyList())
    private val branch = Ref("feature/pages", "refs/heads/feature/pages", head.id, RefType.LOCAL_BRANCH)
    private val nit = ReviewComment(
        id = "c1", filePath = "src/ExpenseStore.kt", startLine = 3, endLine = 3, severity = Severity.BUG,
        category = "correctness", message = "Page 1 skips 10 rows.", originalSnippet = "page * PAGE_SIZE",
        suggestion = "(page - 1) * PAGE_SIZE",
    )
    private val result = ReviewResult("1 bug", listOf(nit))
    private val diff = listOf(FileDiff("src/ExpenseStore.kt", null, FileChangeType.MODIFIED, isBinary = false, hunks = emptyList()))

    private var llmCalls = 0
    private val agent = object : ReviewAgent {
        override suspend fun review(repository: GitRepository, baseRef: String): ReviewResult = result.also { llmCalls++ }
    }

    private class FakeShareClient(private val failing: Boolean = false) : ReviewShareClient {
        val shared = mutableListOf<SharedReview>()
        override suspend fun share(review: SharedReview): String {
            if (failing) throw ReviewShareException("Connection refused")
            shared += review.copy(id = "id-${shared.size + 1}")
            return shared.last().id
        }
        override suspend fun list(repoUrl: String): List<SharedReview> {
            if (failing) throw ReviewShareException("Connection refused")
            return shared.filter { it.repoUrl == repoUrl }.reversed()
        }
        override suspend fun get(id: String): SharedReview? = shared.firstOrNull { it.id == id }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(client: ReviewShareClient, remoteUrl: String? = "https://ghp_secret@github.com/Nitpick-org/pocket-ledger.git") =
        RepositoryViewModel(
            FakeGitRepositoryFactory(
                mapOf(
                    "/work/ledger" to FakeGitRepository(
                        branch = "feature/pages", commits = listOf(head), refs = listOf(branch),
                        diffs = mapOf("main" to diff), remoteUrl = remoteUrl,
                    ),
                ),
            ),
            graphDispatcher = dispatcher,
            reviewAgent = agent,
            shareClient = client,
        ).apply { openRepository("/work/ledger") }

    @Test
    fun sharingSendsTheReviewUnderTheOriginWithoutItsToken() {
        val client = FakeShareClient()
        val viewModel = viewModel(client)
        viewModel.reviewBranch()

        viewModel.shareReview()

        assertEquals(
            SharedReview("id-1", "https://github.com/Nitpick-org/pocket-ledger.git", "feature/pages", "main", head.id, result),
            client.shared.single(),
        )
        assertEquals("id-1", viewModel.state.value.sharedReviewId)
    }

    @Test
    fun aSharedReviewOpensWithItsNitsAndNoLlmCall() {
        val client = FakeShareClient()
        viewModel(client).apply { reviewBranch(); shareReview() }
        val phone = viewModel(client)
        llmCalls = 0

        phone.loadSharedReviews()
        phone.openSharedReview(phone.state.value.sharedReviews.single())

        assertEquals(result, phone.state.value.review)
        assertEquals("id-1", phone.state.value.openedSharedReview?.id)
        assertEquals(0, llmCalls)
    }

    @Test
    fun anUnreachableServerIsAMessageAndTheRestKeepsWorking() {
        val viewModel = viewModel(FakeShareClient(failing = true))
        viewModel.reviewBranch()

        viewModel.shareReview()
        viewModel.loadSharedReviews()

        val state = viewModel.state.value
        assertEquals(ShareFailure.Unreachable, state.shareFailure)
        assertEquals(ShareFailure.Unreachable, state.sharedReviewsFailure)
        assertFalse(state.isSharing)
        assertNull(state.sharedReviewId)
        assertEquals(result, state.review)
        assertEquals(1, state.graph.size)
    }

    @Test
    fun aRepositoryWithoutOriginCannotShare() {
        val viewModel = viewModel(FakeShareClient(), remoteUrl = null)
        viewModel.reviewBranch()

        viewModel.shareReview()

        assertEquals(ShareFailure.NoRemote, viewModel.state.value.shareFailure)
    }
}
