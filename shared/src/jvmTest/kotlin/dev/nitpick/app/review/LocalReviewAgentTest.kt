package dev.nitpick.app.review

import dev.nitpick.app.git.JGitRepositoryFactory
import dev.nitpick.app.git.TestGitRepo
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LocalReviewAgentTest {

    private val repo = TestGitRepo.create()
    private val cleanUp = mutableListOf<File>(repo.dir)

    @AfterTest
    fun deleteRepositories() {
        cleanUp.forEach { it.deleteRecursively() }
    }

    @Test
    fun reviewApplyAndCommitWorkOnARealRepository() = runBlocking<Unit> {
        repo.write("Main.kt", "fun main() {\n}\n")
        repo.commit("base", at = 1_000)
        repo.git("checkout", "-b", "feature")
        repo.write("Main.kt", "fun main() {\n    println(\"debug\")\n}\n")
        repo.commit("feature", at = 2_000)
        val git = JGitRepositoryFactory().open(repo.dir.path)

        val result = LocalReviewAgent().review(git, "main")

        val comment = result.comments.first { it.suggestion != null }
        assertEquals("Main.kt", comment.filePath)
        assertEquals("    println(\"debug\")", comment.originalSnippet)
        val updated = ReviewAnchoring.apply(git.readFile(comment.filePath)!!, comment)
        git.writeFile(comment.filePath, updated)
        git.stage(listOf(comment.filePath))
        git.commit("apply review suggestion")

        assertTrue(git.readFile("Main.kt")!!.contains("//     println(\"debug\")"))
        assertEquals("apply review suggestion", git.log().first().shortMessage)
    }
}
