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
import kotlin.test.assertTrue

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

    // The nit's snippet sits on line 3, where the review put it.
    private val ledgerFiles = mapOf("src/ExpenseStore.kt" to "class ExpenseStore {\n  val PAGE_SIZE = 10\n  page * PAGE_SIZE\n}\n")

    private fun viewModel(
        client: ReviewShareClient,
        remoteUrl: String? = "https://ghp_secret@github.com/Nitpick-org/pocket-ledger.git",
        onBranch: String = "feature/pages",
        files: Map<String, String> = ledgerFiles,
    ) =
        RepositoryViewModel(
            FakeGitRepositoryFactory(
                mapOf(
                    "/work/ledger" to FakeGitRepository(
                        branch = onBranch, commits = listOf(head), refs = listOf(branch),
                        diffs = mapOf("main" to diff), files = files, remoteUrl = remoteUrl,
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
    fun aSharedNitMovesToWhereItsSnippetIsOnThisDevice() {
        val client = FakeShareClient()
        val gone = nit.copy(id = "c2", originalSnippet = "val removed = true")
        client.shared += SharedReview("id-9", "https://github.com/Nitpick-org/pocket-ledger.git", "feature/pages", "main", head.id, ReviewResult("2 nits", listOf(nit, gone)))
        // Two lines were added above the snippet since the review.
        val phone = viewModel(client, files = mapOf("src/ExpenseStore.kt" to "// header\n// header\n" + ledgerFiles.getValue("src/ExpenseStore.kt")))

        phone.loadSharedReviews()
        phone.openSharedReview(phone.state.value.sharedReviews.single())

        val (moved, wholeFile) = phone.state.value.review!!.comments
        assertEquals(5, moved.startLine)
        assertTrue(wholeFile.isFileLevel)
    }

    @Test
    fun aSharedNitOnAnUntrackedOrGitFileIsDropped() {
        val client = FakeShareClient()
        val outside = nit.copy(id = "c3", filePath = "sub/.git/config")
        client.shared += SharedReview("id-7", "https://github.com/Nitpick-org/pocket-ledger.git", "feature/pages", "main", head.id, ReviewResult("2 nits", listOf(nit, outside)))
        val phone = viewModel(client)

        phone.loadSharedReviews()
        phone.openSharedReview(phone.state.value.sharedReviews.single())

        assertEquals(listOf(nit.id), phone.state.value.review!!.comments.map { it.id })
    }

    @Test
    fun aSharedReviewOfAnotherBranchChecksThatBranchOutFirst() {
        val client = FakeShareClient()
        viewModel(client).apply { reviewBranch(); shareReview() }
        val phone = viewModel(client, onBranch = "main")

        phone.loadSharedReviews()
        phone.openSharedReview(phone.state.value.sharedReviews.single())

        assertEquals("feature/pages", phone.state.value.currentBranch)
        assertEquals(result, phone.state.value.review)
    }

    @Test
    fun committingKeepsTheNitsNotAppliedSoTheyCanStillBeShared() {
        val other = nit.copy(id = "c2", startLine = 1, endLine = 1, originalSnippet = "class ExpenseStore {", suggestion = "class ExpenseStore(private val db: Db) {")
        val twoNits = object : ReviewAgent {
            override suspend fun review(repository: GitRepository, baseRef: String) = ReviewResult("2 nits", listOf(nit, other))
        }
        val viewModel = RepositoryViewModel(
            FakeGitRepositoryFactory(
                mapOf("/work/ledger" to FakeGitRepository(branch = "feature/pages", commits = listOf(head), refs = listOf(branch), diffs = mapOf("main" to diff), files = ledgerFiles, remoteUrl = "https://github.com/o/r")),
            ),
            graphDispatcher = dispatcher,
            reviewAgent = twoNits,
            shareClient = FakeShareClient(),
        ).apply { openRepository("/work/ledger") }
        viewModel.reviewBranch()

        viewModel.applySuggestion(nit)
        viewModel.commitAppliedSuggestions()

        assertEquals(listOf("c2"), viewModel.state.value.review?.comments?.map { it.id })
        assertNull(viewModel.state.value.sharedReviewId)
        viewModel.shareReview()
        assertEquals("id-1", viewModel.state.value.sharedReviewId)
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
