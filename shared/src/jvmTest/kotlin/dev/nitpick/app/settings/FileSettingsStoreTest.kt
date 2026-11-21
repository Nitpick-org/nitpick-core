package dev.nitpick.app.settings

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileSettingsStoreTest {

    private fun tempFile(): File =
        File.createTempFile("nitpick-settings", ".json").also { it.delete(); it.deleteOnExit() }

    @Test
    fun roundTripsSettings() = runBlocking {
        val file = tempFile()
        val store = FileSettingsStore(file)
        val settings = Settings(
            provider = LlmProvider.OLLAMA,
            model = "llama3",
            apiKey = "secret",
            themeMode = ThemeMode.DARK,
            recentRepositories = listOf("/tmp/repo"),
        )

        store.save(settings)

        assertTrue(file.isFile)
        assertEquals(settings, store.load())
    }

    @Test
    fun missingFileGivesDefaults() = runBlocking {
        assertEquals(Settings(), FileSettingsStore(tempFile()).load())
    }

    @Test
    fun corruptFileGivesDefaults() = runBlocking {
        val file = tempFile().apply { writeText("{ not json") }
        assertEquals(Settings(), FileSettingsStore(file).load())
    }

    @Test
    fun createsMissingParentDirectory() = runBlocking<Unit> {
        val dir = tempFile()
        val file = File(dir, "nested/settings.json")
        FileSettingsStore(file).save(Settings(themeMode = ThemeMode.LIGHT))
        assertEquals(ThemeMode.LIGHT, FileSettingsStore(file).load().themeMode)
        file.parentFile.deleteRecursively()
    }
}
