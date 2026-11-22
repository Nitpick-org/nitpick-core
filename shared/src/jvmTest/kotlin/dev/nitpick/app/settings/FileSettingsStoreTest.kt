package dev.nitpick.app.settings

import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun aSecondSaveReplacesTheFirst() = runBlocking {
        val file = tempFile()
        val store = FileSettingsStore(file)

        store.save(Settings(apiKey = "first"))
        store.save(Settings(apiKey = "second"))

        assertEquals("second", store.load().apiKey)
        assertFalse(File(file.path + ".tmp").exists())
    }

    @Test
    fun missingFileGivesDefaults() = runBlocking {
        assertEquals(Settings(), FileSettingsStore(tempFile()).load())
    }

    @Test
    fun corruptFileGivesDefaultsAndIsKeptAsABackup() = runBlocking {
        val file = tempFile().apply { writeText("{ not json") }
        assertEquals(Settings(), FileSettingsStore(file).load())
        assertFalse(file.exists())
        assertEquals("{ not json", File(file.path + ".bak").readText())
    }

    @Test
    fun anUnknownEnumValueFallsBackAloneAndKeepsTheKey() = runBlocking {
        val file = tempFile().apply { writeText("""{"provider": "FUTURE_PROVIDER", "apiKey": "secret", "themeMode": "DARK"}""") }

        val loaded = FileSettingsStore(file).load()

        assertEquals("secret", loaded.apiKey)
        assertEquals(ThemeMode.DARK, loaded.themeMode)
        assertEquals(Settings().provider, loaded.provider)
    }

    @Test
    fun createsMissingParentDirectory() = runBlocking<Unit> {
        val dir = tempFile()
        val file = File(dir, "nested/settings.json")
        FileSettingsStore(file).save(Settings(themeMode = ThemeMode.LIGHT))
        assertEquals(ThemeMode.LIGHT, FileSettingsStore(file).load().themeMode)
        file.parentFile.deleteRecursively()
    }

    @Test
    fun savedFileIsReadableByOwnerOnly() = runBlocking {
        val file = tempFile()
        FileSettingsStore(file).save(Settings(apiKey = "secret"))
        val permissions = Files.getPosixFilePermissions(file.toPath())
        assertFalse(GROUP_READ in permissions || OTHERS_READ in permissions)
        assertEquals("secret", FileSettingsStore(file).load().apiKey)
    }

    @Test
    fun emptyModelFromAnOlderFileFallsBackToTheProviderDefault() = runBlocking {
        val file = tempFile().apply { writeText("""{"provider": "OLLAMA", "model": ""}""") }
        assertEquals(LlmProvider.OLLAMA.knownModels.first(), FileSettingsStore(file).load().model)
    }
}
