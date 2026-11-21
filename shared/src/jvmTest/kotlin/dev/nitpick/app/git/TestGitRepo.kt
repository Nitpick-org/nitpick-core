package dev.nitpick.app.git

import java.io.File
import java.nio.file.Files

/**
 * A throwaway repository built with the git CLI, like a user's real repository, so tests
 * never import JGit. Dates are fixed, which keeps commit order deterministic.
 */
class TestGitRepo private constructor(val dir: File) {

    fun write(path: String, content: String) {
        File(dir, path).apply { parentFile.mkdirs() }.writeText(content)
    }

    /** Stages everything, commits, and returns the new commit id. */
    fun commit(message: String, at: Long = BASE_TIME): String {
        git("add", "-A")
        git("commit", "-m", message, at = at)
        return git("rev-parse", "HEAD").trim()
    }

    fun git(vararg args: String, at: Long = BASE_TIME): String {
        val process = ProcessBuilder(listOf("git") + args)
            .directory(dir)
            .redirectErrorStream(true)
            .apply {
                // A git hook (pre-commit runs check.sh) exports GIT_DIR and GIT_INDEX_FILE: inherited,
                // they would point these commands at the real repository instead of this one.
                environment().keys.removeAll { it.startsWith("GIT_") }
                environment()["GIT_CONFIG_NOSYSTEM"] = "1"
                environment()["GIT_CONFIG_GLOBAL"] = emptyConfig.path
                environment()["GIT_AUTHOR_NAME"] = AUTHOR_NAME
                environment()["GIT_AUTHOR_EMAIL"] = AUTHOR_EMAIL
                environment()["GIT_COMMITTER_NAME"] = AUTHOR_NAME
                environment()["GIT_COMMITTER_EMAIL"] = AUTHOR_EMAIL
                environment()["GIT_AUTHOR_DATE"] = "@$at +0000"
                environment()["GIT_COMMITTER_DATE"] = "@$at +0000"
            }
            .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "git ${args.joinToString(" ")} failed:\n$output" }
        return output
    }

    fun delete() {
        dir.deleteRecursively()
    }

    companion object {
        const val BASE_TIME = 1_700_000_000L
        const val AUTHOR_NAME = "Ada Lovelace"
        const val AUTHOR_EMAIL = "ada@nitpick.dev"

        // Ignores the developer's ~/.gitconfig (signing, hooks, default branch).
        private val emptyConfig: File = File.createTempFile("nitpick-gitconfig", "").apply { deleteOnExit() }

        fun create(): TestGitRepo {
            val repo = TestGitRepo(Files.createTempDirectory("nitpick-repo").toFile().canonicalFile)
            repo.git("init", "--initial-branch=main")
            return repo
        }

        /** Runs git commands in an existing folder, such as a bare repository. */
        fun inDir(dir: File): TestGitRepo = TestGitRepo(dir)

        fun cloneOf(origin: TestGitRepo): TestGitRepo {
            val parent = Files.createTempDirectory("nitpick-clone").toFile().canonicalFile
            TestGitRepo(parent).git("clone", origin.dir.path, "clone")
            return TestGitRepo(File(parent, "clone"))
        }
    }
}
