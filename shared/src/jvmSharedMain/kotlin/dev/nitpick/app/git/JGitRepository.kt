package dev.nitpick.app.git

import dev.nitpick.app.diff.parseUnifiedDiff
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.WorkingTreeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.TreeFilter
import java.io.ByteArrayOutputStream
import java.io.File
import org.eclipse.jgit.lib.Ref as JGitRef

/**
 * [GitRepository] backed by JGit, shared by desktop and Android. Every call runs on
 * [Dispatchers.IO] so callers can stay on the UI thread.
 */
class JGitRepository(private val git: Git) : GitRepository {

    private val repository: Repository = git.repository
    private val workTree: File = repository.workTree.canonicalFile

    override suspend fun log(limit: Int): List<Commit> = withContext(Dispatchers.IO) {
        val head = repository.resolve(Constants.HEAD) ?: return@withContext emptyList()
        RevWalk(repository).use { walk ->
            // Topological order puts every child before its parents, which the graph lanes need.
            // shortcut: TOPO walks the whole history before returning, revisit if huge repos get slow.
            walk.sort(RevSort.TOPO, true)
            walk.sort(RevSort.COMMIT_TIME_DESC, true)
            walk.markStart(walk.parseCommit(head))
            walk.take(limit).map { it.toCommit() }
        }
    }

    override suspend fun refs(): List<Ref> = withContext(Dispatchers.IO) {
        repository.refDatabase.refs
            .filterNot { it.isSymbolic } // origin/HEAD only points at another ref
            .mapNotNull { it.toDomainRef() }
            .sortedWith(compareBy({ it.type }, { it.name }))
    }

    override suspend fun currentBranch(): String = withContext(Dispatchers.IO) {
        // Short branch name, or the commit id when HEAD is detached.
        checkNotNull(repository.branch) { "HEAD is missing in ${workTree.path}" }
    }

    override suspend fun status(): WorkingTreeStatus = withContext(Dispatchers.IO) {
        val status = git.status().call()
        WorkingTreeStatus(
            staged = (status.added + status.changed + status.removed).sorted(),
            unstaged = (status.modified + status.missing).sorted(),
            untracked = status.untracked.sorted(),
            conflicted = status.conflicting.sorted(),
        )
    }

    override suspend fun readFile(path: String): String? = withContext(Dispatchers.IO) {
        val file = resolveInWorkTree(path)
        if (file.isFile) file.readText() else null
    }

    override suspend fun listFiles(): List<String> = withContext(Dispatchers.IO) {
        // The index lists tracked files only, so build outputs and ignored files stay out.
        val index = repository.readDirCache()
        (0 until index.entryCount).map { index.getEntry(it).pathString }.distinct()
    }

    /**
     * Compares the fork point with [baseRef] to the working tree, like a pull request would
     * show the branch plus the edits not committed yet (an applied suggestion shows up at once).
     * Untracked files stay out: the review sends every file of this diff to the LLM provider.
     */
    override suspend fun diffAgainst(baseRef: String): List<FileDiff> = withContext(Dispatchers.IO) {
        val base = runCatching { repository.resolve("$baseRef^{commit}") }.getOrNull()
            ?: throw IllegalArgumentException("Unknown base: $baseRef")
        val head = repository.resolve(Constants.HEAD) ?: return@withContext emptyList()
        val output = ByteArrayOutputStream()
        RevWalk(repository).use { walk ->
            val baseCommit = walk.parseCommit(base)
            walk.markStart(walk.parseCommit(head))
            walk.markStart(baseCommit)
            walk.revFilter = RevFilter.MERGE_BASE
            // Unrelated histories have no fork point: compare against the base itself.
            val forkPoint = walk.next() ?: baseCommit
            DiffFormatter(output).use { formatter ->
                formatter.setRepository(repository)
                formatter.isDetectRenames = true
                formatter.pathFilter = TrackedFilter(repository.readDirCache())
                val oldTree = CanonicalTreeParser(null, walk.objectReader, forkPoint.tree)
                // A working tree iterator also makes the formatter skip ignored files.
                // shortcut: hashes every working-tree file per call, add a DirCacheIterator if big repos get slow.
                formatter.format(formatter.scan(oldTree, FileTreeIterator(repository)))
            }
        }
        parseUnifiedDiff(output.toString(Charsets.UTF_8))
    }

    override suspend fun writeFile(path: String, content: String) =
        withContext(Dispatchers.IO) {
            val file = resolveInWorkTree(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
        }

    override suspend fun stage(paths: List<String>) =
        withContext(Dispatchers.IO) {
            paths.forEach { path ->
                val file = resolveInWorkTree(path)
                if (file.exists()) {
                    git.add().addFilepattern(path).call()
                } else {
                    git.rm().addFilepattern(path).call()
                }
            }
        }

    override suspend fun commit(message: String) =
        withContext(Dispatchers.IO) {
            require(message.isNotBlank()) { "Commit message must not be blank" }
            checkNotNull(git.commit().setMessage(message).setSign(false).call()) {
                "Nothing to commit"
            }
            Unit
        }

    override suspend fun fetch(credentials: Credentials?) =
        TODO("fetch is not implemented yet")

    override suspend fun pull(credentials: Credentials?): PullResult =
        TODO("pull is not implemented yet")

    override suspend fun push(credentials: Credentials?) =
        TODO("push is not implemented yet")

    override suspend fun checkout(branch: String) =
        TODO("checkout is not implemented yet")

    /**
     * Paths can come from the LLM, so they must stay inside the working tree and out of
     * `.git`, which may hold credentials in its config.
     */
    private fun resolveInWorkTree(path: String): File {
        require(!File(path).isAbsolute) { "Path must be relative to the repository: $path" }
        val file = File(workTree, path).canonicalFile
        require(file.toPath().startsWith(workTree.toPath())) { "Path escapes the repository: $path" }
        val firstSegment = workTree.toPath().relativize(file.toPath()).firstOrNull()?.toString()
        require(firstSegment != Constants.DOT_GIT) { "Path points inside .git: $path" }
        return file
    }

    private fun RevCommit.toCommit() = Commit(
        id = name,
        shortMessage = shortMessage,
        fullMessage = fullMessage,
        authorName = authorIdent.name,
        authorEmail = authorIdent.emailAddress,
        timestampEpochSeconds = authorIdent.`when`.time / 1000,
        parentIds = parents.map { it.name },
    )

    private fun JGitRef.toDomainRef(): Ref? {
        val type = when {
            name.startsWith(Constants.R_HEADS) -> RefType.LOCAL_BRANCH
            name.startsWith(Constants.R_REMOTES) -> RefType.REMOTE_BRANCH
            name.startsWith(Constants.R_TAGS) -> RefType.TAG
            else -> return null
        }
        // An annotated tag points at a tag object; peeling gives the commit behind it.
        val peeled = repository.refDatabase.peel(this)
        val target = peeled.peeledObjectId ?: peeled.objectId ?: return null
        return Ref(
            name = Repository.shortenRefName(name),
            fullName = name,
            commitId = target.name,
            type = type,
        )
    }
}

/**
 * Keeps paths that git knows: in the index, or in the fork-point tree (tree 0 of the walk),
 * so a file removed with `git rm` still shows as deleted.
 */
private class TrackedFilter(private val index: DirCache) : TreeFilter() {
    override fun include(walker: TreeWalk): Boolean =
        walker.isSubtree || walker.getRawMode(0) != 0 || index.findEntry(walker.rawPath, walker.pathLength) >= 0

    override fun shouldBeRecursive() = false

    override fun clone(): TreeFilter = this
}
