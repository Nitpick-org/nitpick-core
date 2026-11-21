package dev.nitpick.app.git

import dev.nitpick.app.domain.DiffLine
import dev.nitpick.app.domain.DiffLineType
import dev.nitpick.app.domain.FileChangeType
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.WorkingTreeStatus
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
    fun logKeepsChildrenBeforeParentsWhenAClockSkewInvertsCommitTimes() = runBlocking<Unit> {
        repo.write("a.txt", "base")
        repo.commit("base", at = 1_000)
        repo.git("checkout", "-b", "feature")
        repo.write("feature.txt", "feature")
        val onFeature = repo.commit("on feature", at = 3_000)
        repo.git("checkout", "main")
        repo.write("main.txt", "main")
        val onMain = repo.commit("on main", at = 2_000)
        // The merge is older than the feature commit it contains: time order alone would show
        // the parent before the child.
        val merge = repo.git("merge", "--no-ff", "feature", "-m", "merge feature", at = 2_500).let { _ ->
            repo.git("rev-parse", "HEAD").trim()
        }

        val position = open().log().withIndex().associate { (index, commit) -> commit.id to index }

        assertTrue(position.getValue(merge) < position.getValue(onFeature))
        assertTrue(position.getValue(merge) < position.getValue(onMain))
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
    fun diffAgainstABranchWithACleanWorkingTreeStillShowsItsCommits() = runBlocking<Unit> {
        repo.write("a.txt", "1\n")
        repo.commit("base", at = 1_000)
        repo.git("checkout", "-b", "feature")
        repo.write("a.txt", "2\n")
        repo.commit("feature work", at = 2_000)

        val diff = open().diffAgainst("main").single()

        assertEquals("a.txt", diff.path)
        assertEquals(1, diff.addedLines)
        assertEquals(1, diff.removedLines)
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
    fun stageStagesEveryPathInOneCallIncludingDeletions() = runBlocking<Unit> {
        repo.write("a.txt", "a")
        repo.write("b.txt", "b")
        repo.commit("base", at = 1_000)
        val git = open()
        git.writeFile("a.txt", "a2")
        File(repo.dir, "b.txt").delete()

        git.stage(listOf("a.txt", "b.txt"))

        assertEquals(listOf("a.txt", "b.txt"), git.status().staged.sorted())
    }

    @Test
    fun cloneCopiesTheHistoryAndTracksTheOrigin() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        val first = repo.commit("first", at = 1_000)
        repo.write("b.txt", "2")
        val second = repo.commit("second", at = 2_000)
        val target = newCloneTarget()

        val clone = factory.clone(repo.dir.toURI().toString(), target.path, credentials = null)

        assertEquals(listOf(second, first), clone.log().map { it.id })
        assertEquals("main", clone.currentBranch())
        assertEquals("1", clone.readFile("a.txt"))
        assertTrue(clone.refs().any { it.type == RefType.REMOTE_BRANCH && it.name == "origin/main" })
    }

    @Test
    fun cloneOfAnUnreachableUrlFailsAndLeavesNoFolderBehind() = runBlocking<Unit> {
        val target = newCloneTarget()

        assertFailsWith<Exception> {
            factory.clone(File(repo.dir, "missing").toURI().toString(), target.path, credentials = null)
        }

        assertFalse(target.exists())
    }

    @Test
    fun fetchBringsNewRemoteCommitsWithoutMovingTheLocalBranch() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        val first = repo.commit("first", at = 1_000)
        val clone = factory.clone(repo.dir.toURI().toString(), newCloneTarget().path, credentials = null)
        repo.write("a.txt", "2")
        val second = repo.commit("second", at = 2_000)

        clone.fetch(credentials = null)

        val remoteMain = clone.refs().single { it.name == "origin/main" }
        assertEquals(second, remoteMain.commitId)
        assertEquals(listOf(first), clone.log().map { it.id })
    }

    // A path that does not exist yet, so the clone creates it and a failed clone removes it.
    private fun newCloneTarget(): File {
        val parent = Files.createTempDirectory("nitpick-clone").toFile().canonicalFile
        cleanUp += parent
        return File(parent, "repo")
    }

    @Test
    fun pushSendsTheCurrentBranchToTheOrigin() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val bare = newBareOrigin()
        val clone = factory.clone(bare.toURI().toString(), newCloneTarget().path, credentials = null)
        clone.writeFile("a.txt", "2")
        clone.stage(listOf("a.txt"))
        clone.commit("apply review suggestion")

        clone.push(credentials = null)

        val pushed = clone.log().first().id
        assertEquals(pushed, TestGitRepo.inDir(bare).git("rev-parse", "refs/heads/main").trim())
        assertEquals(pushed, clone.refs().single { it.name == "origin/main" }.commitId)
    }

    @Test
    fun pushBehindTheOriginFailsInsteadOfPretendingItWorked() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val bare = newBareOrigin()
        val clone = factory.clone(bare.toURI().toString(), newCloneTarget().path, credentials = null)
        repo.write("a.txt", "from elsewhere")
        repo.commit("second", at = 2_000)
        repo.git("push", bare.path, "main")
        clone.writeFile("a.txt", "from the phone")
        clone.stage(listOf("a.txt"))
        clone.commit("conflicting")

        val error = assertFailsWith<IllegalArgumentException> { clone.push(credentials = null) }

        assertTrue("REJECTED" in error.message.orEmpty(), error.message)
    }

    @Test
    fun publishBranchPushesANewLocalBranchAndSetsItsUpstream() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val bare = newBareOrigin()
        val cloneDir = newCloneTarget()
        val clone = factory.clone(bare.toURI().toString(), cloneDir.path, credentials = null)
        TestGitRepo.inDir(cloneDir).git("checkout", "-b", "feature")
        clone.writeFile("b.txt", "feature")
        clone.stage(listOf("b.txt"))
        clone.commit("feature work")
        assertEquals(null, clone.trackingStatus())

        clone.publishBranch(credentials = null)

        val tracking = clone.trackingStatus()
        assertEquals(0, tracking?.ahead)
        assertEquals(0, tracking?.behind)
        assertEquals("origin/feature", tracking?.upstream)
        assertEquals(clone.log().first().id, TestGitRepo.inDir(bare).git("rev-parse", "refs/heads/feature").trim())
    }

    @Test
    fun trackingStatusReportsAheadAndBehind() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val bare = newBareOrigin()
        val clone = factory.clone(bare.toURI().toString(), newCloneTarget().path, credentials = null)
        clone.writeFile("local.txt", "local")
        clone.stage(listOf("local.txt"))
        clone.commit("local work")

        val ahead = clone.trackingStatus()
        assertEquals(1, ahead?.ahead)
        assertEquals(0, ahead?.behind)
        assertEquals("origin/main", ahead?.upstream)

        repo.write("remote.txt", "remote")
        repo.commit("remote work", at = 2_000)
        repo.git("push", bare.path, "main")
        clone.fetch(credentials = null)

        val diverged = clone.trackingStatus()
        assertEquals(1, diverged?.ahead)
        assertEquals(1, diverged?.behind)
    }

    @Test
    fun checkoutSwitchesToALocalOrARemoteOnlyBranch() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("base", at = 1_000)
        repo.git("checkout", "-b", "feature")
        repo.write("b.txt", "2")
        val onFeature = repo.commit("on feature", at = 2_000)
        repo.git("checkout", "main")
        val clone = factory.clone(repo.dir.toURI().toString(), newCloneTarget().path, credentials = null)
        assertEquals("main", clone.currentBranch())

        clone.checkout("feature")

        assertEquals("feature", clone.currentBranch())
        assertEquals(onFeature, clone.log().first().id)
        assertEquals("2", clone.readFile("b.txt"))
        clone.checkout("main")
        assertEquals("main", clone.currentBranch())
    }

    // A bare repository seeded from `repo`, playing the GitHub side of a push.
    private fun newBareOrigin(): File {
        val bare = Files.createTempDirectory("nitpick-bare").toFile().canonicalFile
        cleanUp += bare
        // A bare HEAD points at master by default; the clone needs it on the branch we push.
        repo.git("init", "--bare", "--initial-branch=main", bare.path)
        repo.git("push", bare.path, "main")
        return bare
    }

    @Test
    fun cloneWritesTheCommitIdentityIntoTheCloneConfig() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val cloneDir = newCloneTarget()
        val factory = JGitRepositoryFactory(GitIdentity("Nitpick on Android", "nitpick-android@example.com"))

        val clone = factory.clone(repo.dir.toURI().toString(), cloneDir.path, credentials = null)

        // The clone-local config wins over the developer's gitconfig for commits made in this clone.
        assertEquals("Nitpick on Android", TestGitRepo.inDir(cloneDir).git("config", "--local", "user.name").trim())
        clone.writeFile("a.txt", "2")
        clone.stage(listOf("a.txt"))
        clone.commit("from the phone")
        assertEquals("nitpick-android@example.com", clone.log().first().authorEmail)
    }

    @Test
    fun pullFastForwardsToTheOrigin() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val bare = newBareOrigin()
        val cloneDir = newCloneTarget()
        val clone = factory.clone(bare.toURI().toString(), cloneDir.path, credentials = null)
        // A developer's gitconfig may ask for rebase pulls; Nitpick always merges.
        TestGitRepo.inDir(cloneDir).git("config", "pull.rebase", "true")
        repo.write("a.txt", "2")
        val second = repo.commit("second", at = 2_000)
        repo.git("push", bare.path, "main")

        val result = clone.pull(credentials = null)

        assertEquals(PullResult(updated = true, conflictedPaths = emptyList()), result)
        assertEquals(second, clone.log().first().id)
        assertEquals("2", clone.readFile("a.txt"))
    }

    @Test
    fun pullWithNothingNewReportsNoUpdate() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        val first = repo.commit("first", at = 1_000)
        val clone = factory.clone(newBareOrigin().toURI().toString(), newCloneTarget().path, credentials = null)

        val result = clone.pull(credentials = null)

        assertEquals(PullResult(updated = false, conflictedPaths = emptyList()), result)
        assertEquals(first, clone.log().first().id)
    }

    @Test
    fun pullInConflictIsCancelledAndLeavesTheTreeUntouched() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("first", at = 1_000)
        val bare = newBareOrigin()
        val cloneDir = newCloneTarget()
        val clone = factory.clone(bare.toURI().toString(), cloneDir.path, credentials = null)
        repo.write("a.txt", "from elsewhere")
        repo.commit("second", at = 2_000)
        repo.git("push", bare.path, "main")
        clone.writeFile("a.txt", "from the phone")
        clone.stage(listOf("a.txt"))
        clone.commit("local change")
        val localHead = clone.log().first().id

        val result = clone.pull(credentials = null)

        assertEquals(PullResult(updated = false, conflictedPaths = listOf("a.txt")), result)
        assertEquals(localHead, clone.log().first().id)
        assertEquals("from the phone", clone.readFile("a.txt"))
        assertEquals(WorkingTreeStatus(emptyList(), emptyList(), emptyList(), emptyList()), clone.status())
        // No merge in progress either: the next commit must not silently become a merge commit.
        assertFalse(File(cloneDir, ".git/MERGE_HEAD").exists())
    }

    @Test
    fun commitWithNothingStagedFailsInsteadOfCreatingAnEmptyCommit() = runBlocking<Unit> {
        repo.write("a.txt", "1")
        repo.commit("base")
        val before = repo.git("rev-parse", "HEAD").trim()

        assertFails { open().commit("empty") }

        assertEquals(before, repo.git("rev-parse", "HEAD").trim())
    }
}
