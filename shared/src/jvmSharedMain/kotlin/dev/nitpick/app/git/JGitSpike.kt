package dev.nitpick.app.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.PersonIdent
import java.io.File

// shortcut: H1 spike proving JGit runs on desktop and Android, delete once JGitRepository lands.
fun jgitSmokeLog(dir: File): List<String> {
    val author = PersonIdent("Nitpick", "spike@nitpick.dev")
    Git.init().setDirectory(dir).setInitialBranch("main").call().use { git ->
        File(dir, "README.md").writeText("hello\n")
        git.add().addFilepattern("README.md").call()
        git.commit()
            .setMessage("first commit")
            .setAuthor(author)
            .setCommitter(author)
            .setSign(false)
            .call()
        return git.log().call().map { it.shortMessage }
    }
}
