package dev.nitpick.app.git

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.WorkingTreeStatus
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JGitRepositoryTest {

    private val repo = TestGitRepo.create()
    private val cleanUp = mutableListOf<File>(repo.dir)
    private val factory = JGitRepositoryFactory()

    @AfterTest
    fun deleteRepositories() {
        cleanUp.forEach { it.deleteRecursively() }
    }

    private fun open(dir: File = repo.dir): GitRepository = runBlocking { factory.open(dir.path) }

    @Test
    fun logReturnsTheNewestCommitsFirstWithinTheLimit() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        repo.write("a.txt", "2")
        val second = repo.commit("second", at = 2_000)
        repo.write("a.txt", "3")
        val third = repo.commit("third\n\nwith a body", at = 3_000)

        val commits = open().log(limit = 2)

        assertEquals(listOf(third, second), commits.map { it.id })
        val newest = commits.first()
        assertEquals("third", newest.shortMessage)
        assertEquals("third\n\nwith a body\n", newest.fullMessage)
        assertEquals(TestGitRepo.AUTHOR_NAME, newest.authorName)
        assertEquals(TestGitRepo.AUTHOR_EMAIL, newest.authorEmail)
        assertEquals(3_000, newest.timestampEpochSeconds)
        assertEquals(listOf(second), newest.parentIds)
    }

    @Test
    fun logListsBothParentsOfAMergeAndChildrenBeforeParents() = runBlocking<Unit> {
        repo.write("a.txt", "base")
        repo.commit("base", at = 1_000)
        repo.git("checkout", "-b", "feature")
        repo.write("feature.txt", "feature")
        val onFeature = repo.commit("on feature", at = 2_000)
        repo.git("checkout", "main")
        repo.write("main.txt", "main")
        val onMain = repo.commit("on main", at = 3_000)
        repo.git("merge", "--no-ff", "feature", "-m", "merge feature", at = 4_000)

        val commits = open().log()

        assertEquals(4, commits.size)
        assertEquals(listOf(onMain, onFeature), commits.first().parentIds)
        val position = commits.withIndex().associate { (index, commit) -> commit.id to index }
        commits.forEach { commit ->
            commit.parentIds.forEach { parent -> assertTrue(position.getValue(commit.id) < position.getValue(parent)) }
        }
    }

    @Test
    fun logOfARepositoryWithoutCommitsIsEmpty() = runBlocking<Unit> {
        assertEquals(emptyList(), open().log())
    }

    @Test
    fun refsListLocalBranchesRemoteBranchesAndPeeledTags() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        val first = repo.commit("first")
        repo.git("branch", "feature")
        repo.git("tag", "-a", "v1", "-m", "release 1")
        repo.write("a.txt", "2")
        val second = repo.commit("second")
        repo.git("tag", "light")
        val clone = TestGitRepo.cloneOf(repo)
        cleanUp += clone.dir.parentFile

        val refs = open(clone.dir).refs()

        assertEquals(
            listOf(
                Ref("main", "refs/heads/main", second, RefType.LOCAL_BRANCH),
                Ref("origin/feature", "refs/remotes/origin/feature", first, RefType.REMOTE_BRANCH),
                Ref("origin/main", "refs/remotes/origin/main", second, RefType.REMOTE_BRANCH),
                Ref("light", "refs/tags/light", second, RefType.TAG),
                Ref("v1", "refs/tags/v1", first, RefType.TAG),
            ),
            refs,
        )
    }

    @Test
    fun currentBranchFollowsCheckout() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first")
        val git = open()
        assertEquals("main", git.currentBranch())

        repo.git("checkout", "-b", "feature")

        assertEquals("feature", git.currentBranch())
    }

    @Test
    fun statusSeparatesStagedUnstagedAndUntrackedFiles() = runBlocking<Unit> {
        repo.write("modified.txt", "1")
        repo.write("kept.txt", "1")
        repo.commit("first")
        assertTrue(open().status().isClean)

        repo.write("modified.txt", "2")
        repo.write("added.txt", "new")
        repo.git("add", "added.txt")
        repo.write("untracked.txt", "new")

        assertEquals(
            WorkingTreeStatus(
                staged = listOf("added.txt"),
                unstaged = listOf("modified.txt"),
                untracked = listOf("untracked.txt"),
                conflicted = emptyList(),
            ),
            open().status(),
        )
    }

    @Test
    fun readFileReturnsTheWorkingTreeContentOrNullWhenMissing() = runBlocking<Unit> {
        repo.write("src/Main.kt", "fun main() {}\n")
        repo.commit("first")
        val git = open()

        assertEquals("fun main() {}\n", git.readFile("src/Main.kt"))
        assertNull(git.readFile("missing.kt"))
        assertNull(git.readFile("src"))
    }

    @Test
    fun readFileRejectsPathsOutsideTheRepositoryOrInsideDotGit() = runBlocking<Unit> {
        val git = open()

        listOf("../secret.txt", "src/../../secret.txt", "/etc/passwd", ".git/config").forEach { path ->
            assertFailsWith<IllegalArgumentException>(path) { git.readFile(path) }
        }
    }

    @Test
    fun listFilesListsTrackedFilesOnly() = runBlocking<Unit> {
        repo.write("README.md", "# Demo")
        repo.write("src/Main.kt", "fun main() {}")
        repo.commit("first")
        repo.write("scratch.txt", "not tracked")

        assertEquals(listOf("README.md", "src/Main.kt"), open().listFiles())
    }

    @Test
    fun openAcceptsASubfolderOfARepository() = runBlocking<Unit> {
        repo.write("src/Main.kt", "fun main() {}")
        val first = repo.commit("first")

        assertEquals(listOf(first), open(File(repo.dir, "src")).log().map { it.id })
    }

    @Test
    fun openRejectsAFolderOutsideAnyRepository() {
        val folder = Files.createTempDirectory("nitpick-not-a-repo").toFile()
        cleanUp += folder

        assertFailsWith<IllegalArgumentException> { open(folder) }
    }

    @Test
    fun openExplainsThatLinkedWorktreesAreNotSupportedYet() {
        repo.write("a.txt", "1")
        repo.commit("first")
        val parent = Files.createTempDirectory("nitpick-worktree").toFile().canonicalFile
        cleanUp += parent
        val worktree = File(parent, "wt-feature")
        repo.git("worktree", "add", "-b", "feature", worktree.path)

        val error = assertFailsWith<IllegalArgumentException> { open(worktree) }

        assertTrue(error.message.orEmpty().contains("worktree"), error.message)
    }

    @Test
    fun diffAgainstShowsTheBranchWorkSinceItLeftTheBase() = runBlocking<Unit> {
        repo.write("a.txt", "1\n2\n3\n4\n5\n")
        repo.write("old.txt", "kept as is\n")
        repo.write(".gitignore", "build/\n")
        repo.commit("base", at = 1_000)
        repo.git("checkout", "-b", "feature")
        repo.write("a.txt", "1\n2\n3\nfour\n5\n")
        repo.git("mv", "old.txt", "new.txt")
        repo.commit("feature work", at = 2_000)
        // main moves on after the fork: its new file is not part of the branch's work.
        repo.git("checkout", "main")
        repo.write("main-only.txt", "x\n")
        repo.commit("main moves on", at = 3_000)
        repo.git("checkout", "feature")
        // Staged but not committed yet: part of the branch's work.
        repo.write("draft.txt", "wip\n")
        repo.git("add", "draft.txt")
        // Untracked: may hold secrets, so it must never reach the review.
        repo.write("secrets.txt", "KEY=secret\n")
        repo.write("build/out.txt", "ignored\n")

        val diff = open().diffAgainst("main").associateBy { it.path }

        assertEquals(setOf("a.txt", "new.txt", "draft.txt"), diff.keys)
        assertEquals(FileChangeType.RENAMED, diff.getValue("new.txt").changeType)
        assertEquals("old.txt", diff.getValue("new.txt").oldPath)
        assertEquals(FileChangeType.ADDED, diff.getValue("draft.txt").changeType)
        val lines = diff.getValue("a.txt").hunks.single().lines
        assertEquals(DiffLine(DiffLineType.REMOVED, "4", oldLineNumber = 4, newLineNumber = null), lines.single { it.type == DiffLineType.REMOVED })
        assertEquals(DiffLine(DiffLineType.ADDED, "four", oldLineNumber = null, newLineNumber = 4), lines.single { it.type == DiffLineType.ADDED })
    }

    @Test
    fun diffAgainstTheBaseItselfIsEmpty() = runBlocking<Unit> {
        repo.write("a.txt", "1\n")
        repo.commit("base")

        assertEquals(emptyList(), open().diffAgainst("main"))
    }

    @Test
    fun diffAgainstAnUnknownBaseFails() = runBlocking<Unit> {
        repo.write("a.txt", "1\n")
        repo.commit("base")

        val error = assertFailsWith<IllegalArgumentException> { open().diffAgainst("develop") }

        assertTrue(error.message.orEmpty().contains("develop"), error.message)
    }

    @Test
    fun writeStageAndCommitUpdateTheGraph() = runBlocking<Unit> {
        repo.write("a.txt", "one\n")
        repo.commit("base", at = 1_000)
        val git = open()

        git.writeFile("a.txt", "one\ntwo\n")
        git.stage(listOf("a.txt"))
        git.commit("apply review suggestion")

        val commits = git.log()
        assertEquals("apply review suggestion", commits.first().shortMessage)
        assertEquals("one\ntwo\n", git.readFile("a.txt"))
    }

    @Test
    fun networkOperationsAreNotImplementedYet() = runBlocking<Unit> {
        assertFailsWith<NotImplementedError> { open().push(credentials = null) }
    }
}
