package dev.nitpick.app.git

import dev.nitpick.app.diff.parseUnifiedDiff
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import dev.nitpick.app.domain.TrackingStatus
import dev.nitpick.app.domain.WorkingTreeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.MergeResult
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.errors.CheckoutConflictException
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.BranchTrackingStatus
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.eclipse.jgit.treewalk.filter.PathFilterGroup
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.StandardCopyOption
import java.nio.file.Files
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.charset.CodingErrorAction
import java.nio.charset.CharacterCodingException
import java.nio.ByteBuffer
import java.io.OutputStream
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
            // Walking in commit-time order stops at [limit] without buffering the whole history
            // (TOPO used to walk every ancestor first: ~450 ms on a 100k-commit repo). The graph
            // lanes still need every child before its parents, so the window is topo-sorted
            // in memory: it is bounded by [limit], not by the repository size.
            walk.sort(RevSort.COMMIT_TIME_DESC, true)
            walk.markStart(walk.parseCommit(head))
            val window = walk.take(limit).toList()
            childrenBeforeParents(window).map { it.toCommit() }
        }
    }

    /** Kahn's algorithm on the child -> parent edges of a newest-first window. */
    private fun childrenBeforeParents(window: List<RevCommit>): List<RevCommit> {
        if (window.size <= 1) return window
        val byId = window.associateBy { it.name }
        val childCount = HashMap<String, Int>(window.size)
        window.forEach { commit ->
            commit.parents.forEach { parent ->
                if (parent.name in byId) childCount[parent.name] = (childCount[parent.name] ?: 0) + 1
            }
        }
        val ready = ArrayDeque(window.filter { (childCount[it.name] ?: 0) == 0 })
        val ordered = ArrayList<RevCommit>(window.size)
        while (ready.isNotEmpty()) {
            val commit = ready.removeFirst()
            ordered += commit
            for (parent in commit.parents) {
                val remaining = (childCount[parent.name] ?: continue) - 1
                if (remaining == 0) {
                    childCount.remove(parent.name)
                    byId[parent.name]?.let { ready.addLast(it) }
                } else {
                    childCount[parent.name] = remaining
                }
            }
        }
        // A git history is a DAG, so this only guards against a truncated window.
        if (ordered.size < window.size) {
            val seen = ordered.mapTo(HashSet(window.size)) { it.name }
            ordered += window.filterNot { it.name in seen }
        }
        return ordered
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

    override suspend fun remoteUrl(): String? = withContext(Dispatchers.IO) {
        repository.config.getString("remote", Constants.DEFAULT_REMOTE_NAME, "url")
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
        if (!file.isFile) return@withContext null
        // Strict decoding: a non-UTF-8 file must fail here, or Apply would write replacement
        // characters over every byte the lenient decoder could not read.
        val decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        try {
            decoder.decode(ByteBuffer.wrap(file.readBytes())).toString()
        } catch (e: CharacterCodingException) {
            throw IllegalArgumentException("Not a UTF-8 text file: $path", e)
        }
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
            val headCommit = walk.parseCommit(head)
            walk.markStart(headCommit)
            walk.markStart(baseCommit)
            walk.revFilter = RevFilter.MERGE_BASE
            // Unrelated histories have no fork point: compare against the base itself.
            val forkPoint = walk.next() ?: baseCommit
            val workingTreeChanged = workingTreeChangedPaths()
            val forkTree = CanonicalTreeParser(null, walk.objectReader, forkPoint.tree)
            DiffFormatter(output).use { formatter ->
                formatter.setRepository(repository)
                formatter.isDetectRenames = true
                // Quoted paths (`"a/caf\303\251.kt"`) would reach the parser verbatim and Apply could not find the file.
                formatter.setQuotePaths(false)
                // A clean working tree is just a tree-to-tree diff: no file is hashed at all.
                if (workingTreeChanged.isEmpty()) {
                    val headTree = CanonicalTreeParser(null, walk.objectReader, headCommit.tree)
                    formatter.format(formatter.scan(forkTree, headTree))
                } else {
                    // Comparing the working tree against a tree walks and hashes every tracked
                    // file. Restrict the walk to the paths that differ from the fork point or
                    // from HEAD, so the cost follows the branch, not the repository.
                    val changed = changedPathsBetween(forkTree, headCommit, workingTreeChanged, walk)
                    formatter.pathFilter = PathFilterGroup.createFromStrings(changed)
                    val workingForkTree = CanonicalTreeParser(null, walk.objectReader, forkPoint.tree)
                    formatter.format(formatter.scan(workingForkTree, FileTreeIterator(repository)))
                }
            }
        }
        parseUnifiedDiff(output.toString(Charsets.UTF_8))
    }

    /** Paths changed between the fork point and HEAD, plus the files the index or worktree changed. */
    private fun changedPathsBetween(
        forkTree: CanonicalTreeParser,
        headCommit: RevCommit,
        workingTreeChanged: Set<String>,
        walk: RevWalk,
    ): Set<String> {
        val changed = workingTreeChanged.toMutableSet()
        val headTree = CanonicalTreeParser(null, walk.objectReader, headCommit.tree)
        DiffFormatter(OutputStream.nullOutputStream()).use { formatter ->
            formatter.setRepository(repository)
            formatter.isDetectRenames = true
            formatter.scan(forkTree, headTree).forEach { header ->
                if (header.oldPath != DiffEntry.DEV_NULL) changed += header.oldPath
                if (header.newPath != DiffEntry.DEV_NULL) changed += header.newPath
            }
        }
        return changed
    }

    /** Tracked paths git considers changed, staged or not; untracked files stay out of the diff. */
    private fun workingTreeChangedPaths(): Set<String> {
        val status = git.status().call()
        return buildSet {
            addAll(status.added)
            addAll(status.changed)
            addAll(status.removed)
            addAll(status.modified)
            addAll(status.missing)
            addAll(status.conflicting)
        }
    }

    override suspend fun writeFile(path: String, content: String) =
        withContext(Dispatchers.IO) {
            val file = resolveInWorkTree(path)
            file.parentFile?.mkdirs()
            // Write beside the file, then rename: a crash mid-write never leaves a truncated file.
            val temporary = File(file.parentFile, ".${file.name}.nitpick-tmp")
            temporary.writeText(content)
            try {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Unit
        }

    override suspend fun stage(paths: List<String>) =
        withContext(Dispatchers.IO) {
            // One index write for all the files: the previous loop rewrote the index per path.
            val (existing, missing) = paths.distinct().partition { resolveInWorkTree(it).exists() }
            if (existing.isNotEmpty()) {
                git.add().apply { existing.forEach { addFilepattern(it) } }.call()
            }
            if (missing.isNotEmpty()) {
                git.rm().apply { missing.forEach { addFilepattern(it) } }.call()
            }
        }

    override suspend fun commit(message: String, paths: List<String>) =
        withContext(Dispatchers.IO) {
            require(message.isNotBlank()) { "Commit message must not be blank" }
            // JGit commits an unchanged index by default, unlike the git CLI. With paths, only
            // those land in the commit: a file the user staged by hand stays staged.
            val command = git.commit().setMessage(message).setSign(false).setAllowEmpty(false)
            paths.distinct().forEach { command.setOnly(it) }
            command.call()
            Unit
        }

    override suspend fun fetch(credentials: Credentials?) {
        withContext(Dispatchers.IO) {
            requireSupportedRemote()
            git.fetch().setCredentialsProvider(credentials.toJGit()).call()
        }
    }

    override suspend fun pull(credentials: Credentials?): PullResult = withContext(Dispatchers.IO) {
        // A hard reset after a conflicted merge is only safe on a clean tree. With local edits,
        // allow a fast-forward only; a divergent history aborts before touching anything.
        requireSupportedRemote()
        val uncommitted = git.status().call().uncommittedChanges
        val dirty = uncommitted.isNotEmpty()
        val command = git.pull()
            .setRebase(false)
            .setCredentialsProvider(credentials.toJGit())
        if (dirty) command.setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
        val merge = try {
            command.call().mergeResult
        } catch (e: CheckoutConflictException) {
            // The incoming commits change a file with local edits, and JGit wrote nothing. Any
            // other failure (network, credentials) keeps its own message.
            throw IllegalStateException(commitOrDiscardFirst(e.conflictingPaths.orEmpty()), e)
        }
        // Fast-forward only over a diverged history: JGit answers ABORTED without touching a file.
        if (dirty && merge.mergeStatus == MergeResult.MergeStatus.ABORTED) error(commitOrDiscardFirst(uncommitted))
        when (merge.mergeStatus) {
            MergeResult.MergeStatus.CONFLICTING -> {
                check(!dirty) { commitOrDiscardFirst(uncommitted) }
                // Conflict resolution is out of scope: put the tree back exactly as it was.
                git.reset().setMode(ResetCommand.ResetType.HARD).call()
                PullResult(updated = false, conflictedPaths = merge.conflicts.keys.sorted())
            }
            MergeResult.MergeStatus.ALREADY_UP_TO_DATE -> PullResult(updated = false, conflictedPaths = emptyList())
            MergeResult.MergeStatus.FAST_FORWARD, MergeResult.MergeStatus.MERGED ->
                PullResult(updated = true, conflictedPaths = emptyList())
            else -> error("Pull failed: ${merge.mergeStatus}")
        }
    }

    override suspend fun push(credentials: Credentials?) {
        withContext(Dispatchers.IO) {
            requireSupportedRemote()
            requireAccepted(git.push().setCredentialsProvider(credentials.toJGit()).call())
        }
    }

    override suspend fun publishBranch(credentials: Credentials?) = withContext(Dispatchers.IO) {
        requireSupportedRemote()
        val branch = currentLocalBranch() ?: throw IllegalStateException("Cannot publish a detached HEAD")
        requireAccepted(
            git.push()
                .setRemote(Constants.DEFAULT_REMOTE_NAME)
                .setRefSpecs(RefSpec("refs/heads/$branch:refs/heads/$branch"))
                .setCredentialsProvider(credentials.toJGit())
                .call(),
        )
        val config = repository.config
        config.setString("branch", branch, "remote", Constants.DEFAULT_REMOTE_NAME)
        config.setString("branch", branch, "merge", "refs/heads/$branch")
        config.save()
    }

    // JGit reports a rejected ref in the result instead of throwing, unlike the git CLI.
    private fun requireAccepted(results: Iterable<PushResult>) {
        val rejected = results.flatMap { it.remoteUpdates }.filterNot { it.status in ACCEPTED_PUSH_STATUSES }
        require(rejected.isEmpty()) {
            rejected.joinToString("; ") { "${it.remoteName}: ${it.status}${it.message?.let { m -> " ($m)" } ?: ""}" }
        }
    }

    override suspend fun trackingStatus(): TrackingStatus? = withContext(Dispatchers.IO) {
        val branch = currentLocalBranch() ?: return@withContext null
        val tracking = BranchTrackingStatus.of(repository, branch) ?: return@withContext null
        TrackingStatus(
            ahead = tracking.aheadCount,
            behind = tracking.behindCount,
            upstream = Repository.shortenRefName(tracking.remoteTrackingBranch),
        )
    }

    override suspend fun checkout(branch: String) {
        withContext(Dispatchers.IO) {
            val local = repository.findRef(Constants.R_HEADS + branch)
            if (local != null) {
                git.checkout().setName(branch).call()
            } else {
                // A branch that only exists on the remote gets a tracking local branch, like `git checkout`.
                git.checkout()
                    .setCreateBranch(true)
                    .setName(branch)
                    .setStartPoint(Constants.DEFAULT_REMOTE_NAME + "/" + branch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .call()
            }
        }
    }

    /** The short branch name, or null when HEAD is detached (`repository.branch` would give the commit id). */
    private fun currentLocalBranch(): String? =
        repository.fullBranch?.takeIf { it.startsWith(Constants.R_HEADS) }?.let { Repository.shortenRefName(it) }

    // The build ships core JGit without an SSH transport: an SSH remote must fail with a
    // sentence, not JGit's "remote hung up unexpectedly" (#232).
    private fun requireSupportedRemote() {
        val url = repository.config.getString("remote", Constants.DEFAULT_REMOTE_NAME, "url") ?: return
        check(!isSshRemote(url)) { UNSUPPORTED_SSH_REMOTE }
    }

    /**
     * Paths can come from the LLM, so they must stay inside the working tree and out of
     * `.git`, which may hold credentials in its config.
     */
    private fun resolveInWorkTree(path: String): File {
        require(!File(path).isAbsolute) { "Path must be relative to the repository: $path" }
        val file = File(workTree, path).canonicalFile
        require(file.toPath().startsWith(workTree.toPath())) { "Path escapes the repository: $path" }
        val insideGitDir = workTree.toPath().relativize(file.toPath()).any { it.toString() == Constants.DOT_GIT }
        require(!insideGitDir) { "Path points inside .git: $path" }
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

private val ACCEPTED_PUSH_STATUSES = setOf(RemoteRefUpdate.Status.OK, RemoteRefUpdate.Status.UP_TO_DATE)

internal const val UNSUPPORTED_SSH_REMOTE = "SSH remotes are not supported yet: use an HTTPS remote with a token"

private val SSH_REMOTE = Regex("""^[\w.-]+@[^:/]+:""")

internal fun isSshRemote(url: String): Boolean = url.startsWith("ssh://") || SSH_REMOTE.containsMatchIn(url)

private fun commitOrDiscardFirst(paths: Collection<String>): String =
    "Commit or discard your changes before pulling: ${paths.sorted().joinToString(", ")}"
