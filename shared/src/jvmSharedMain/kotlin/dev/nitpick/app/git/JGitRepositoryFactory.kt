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
        requireNotNull(builder.gitDir) { "Not a git repository: $path" }
        val repository = builder.setMustExist(true).build()
        if (repository.isBare) {
            repository.close()
            throw IllegalArgumentException("Bare repositories are not supported: $path")
        }
        JGitRepository(Git.wrap(repository))
    }

    override suspend fun clone(url: String, path: String, credentials: Credentials?): GitRepository =
        TODO("clone is not implemented yet")
}
