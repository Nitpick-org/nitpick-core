package dev.nitpick.app.git

import dev.nitpick.app.domain.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File

class JGitRepositoryFactory : GitRepositoryFactory {

    override suspend fun open(path: String): GitRepository = withContext(Dispatchers.IO) {
        // findGitDir walks up the parent folders, so picking a subfolder of a repository works.
        val builder = FileRepositoryBuilder().findGitDir(File(path))
        val gitDir = requireNotNull(builder.gitDir) { "Not a git repository: $path" }
        // A linked worktree (`git worktree add`) has a `commondir` file; JGit 6.10 cannot open it.
        require(!File(gitDir, "commondir").isFile) {
            "Git worktrees are not supported yet, open the main repository folder instead: $path"
        }
        val repository = builder.setMustExist(true).build()
        if (repository.isBare) {
            repository.close()
            throw IllegalArgumentException("Bare repositories are not supported: $path")
        }
        JGitRepository(Git.wrap(repository))
    }

    override suspend fun clone(url: String, path: String, credentials: Credentials?): GitRepository =
        withContext(Dispatchers.IO) {
            // JGit deletes a folder it created itself when the clone fails, so a retry can reuse the path.
            // Without an explicit gitDir, JGit takes GIT_DIR from the environment (set by git hooks)
            // and would initialise the clone inside the caller's repository.
            val git = Git.cloneRepository()
                .setURI(url)
                .setDirectory(File(path))
                .setGitDir(File(path, ".git"))
                .setCredentialsProvider(credentials.toJGit())
                .call()
            JGitRepository(git)
        }
}
