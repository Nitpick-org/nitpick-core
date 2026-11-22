package dev.nitpick.app.git

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.WorkingTreeStatus

/** In-memory [GitRepository] for tests of anything built on top of the git contract. */
class FakeGitRepository(
    private var branch: String = "main",
    private val commits: List<Commit> = emptyList(),
    private val refs: List<Ref> = emptyList(),
    private val diffs: Map<String, List<FileDiff>> = emptyMap(),
    files: Map<String, String> = emptyMap(),
    private val remoteUrl: String? = null,
    var checkoutError: Exception? = null,
) : GitRepository {
    val files: MutableMap<String, String> = files.toMutableMap()
    val staged = mutableListOf<String>()
    val commitMessages = mutableListOf<String>()

    override suspend fun log(limit: Int): List<Commit> = commits.take(limit)
    override suspend fun refs(): List<Ref> = refs
    override suspend fun currentBranch(): String = branch
    override suspend fun remoteUrl(): String? = remoteUrl
    override suspend fun status() = WorkingTreeStatus(emptyList(), emptyList(), emptyList(), emptyList())
    override suspend fun readFile(path: String): String? = files[path]
    override suspend fun listFiles(): List<String> = files.keys.sorted()
    override suspend fun diffAgainst(baseRef: String): List<FileDiff> =
        diffs[baseRef] ?: throw IllegalArgumentException("Unknown base: $baseRef")
    override suspend fun writeFile(path: String, content: String) { files[path] = content }
    override suspend fun stage(paths: List<String>) { staged += paths }
    val committedPaths = mutableListOf<List<String>>()
    override suspend fun commit(message: String, paths: List<String>) { commitMessages += message; committedPaths += paths }
    override suspend fun fetch(credentials: Credentials?) = notFaked("fetch")
    override suspend fun pull(credentials: Credentials?): PullResult = notFaked("pull")
    override suspend fun push(credentials: Credentials?) = notFaked("push")
    override suspend fun checkout(branch: String) {
        checkoutError?.let { throw it }
        this.branch = branch
    }
}

/** Opens [repositories] by path; any other path fails like a folder that is not a repository. */
class FakeGitRepositoryFactory(
    private val repositories: Map<String, GitRepository>,
) : GitRepositoryFactory {
    override suspend fun open(path: String): GitRepository =
        repositories[path] ?: throw IllegalArgumentException("Not a git repository: $path")

    override suspend fun clone(url: String, path: String, credentials: Credentials?): GitRepository =
        notFaked("clone")
}

private fun notFaked(operation: String): Nothing =
    throw UnsupportedOperationException("$operation is not faked by this test double")
