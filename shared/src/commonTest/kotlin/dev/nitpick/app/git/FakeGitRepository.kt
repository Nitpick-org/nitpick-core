package dev.nitpick.app.git

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.WorkingTreeStatus

/** In-memory [GitRepository] for tests of anything built on top of the git contract. */
class FakeGitRepository(
    private val branch: String = "main",
    private val commits: List<Commit> = emptyList(),
    private val refs: List<Ref> = emptyList(),
) : GitRepository {
    override suspend fun log(limit: Int): List<Commit> = commits.take(limit)
    override suspend fun refs(): List<Ref> = refs
    override suspend fun currentBranch(): String = branch
    override suspend fun status() = WorkingTreeStatus(emptyList(), emptyList(), emptyList(), emptyList())
    override suspend fun readFile(path: String): String? = TODO("not faked yet")
    override suspend fun listFiles(): List<String> = TODO("not faked yet")
    override suspend fun diffAgainst(baseRef: String): List<FileDiff> = TODO("not faked yet")
    override suspend fun writeFile(path: String, content: String) = TODO("not faked yet")
    override suspend fun stage(paths: List<String>) = TODO("not faked yet")
    override suspend fun commit(message: String) = TODO("not faked yet")
    override suspend fun fetch(credentials: Credentials?) = TODO("not faked yet")
    override suspend fun pull(credentials: Credentials?): PullResult = TODO("not faked yet")
    override suspend fun push(credentials: Credentials?) = TODO("not faked yet")
    override suspend fun checkout(branch: String) = TODO("not faked yet")
}

/** Opens [repositories] by path; any other path fails like a folder that is not a repository. */
class FakeGitRepositoryFactory(
    private val repositories: Map<String, GitRepository>,
) : GitRepositoryFactory {
    override suspend fun open(path: String): GitRepository =
        repositories[path] ?: throw IllegalArgumentException("Not a git repository: $path")

    override suspend fun clone(url: String, path: String, credentials: Credentials?): GitRepository =
        TODO("not faked yet")
}
