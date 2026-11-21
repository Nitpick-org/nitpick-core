package dev.nitpick.app.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class FileSettingsStore(private val file: File) : SettingsStore {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    override suspend fun load(): Settings = withContext(Dispatchers.IO) {
        if (!file.isFile) return@withContext Settings()
        try {
            json.decodeFromString(Settings.serializer(), file.readText())
        } catch (e: SerializationException) {
            Settings()
        } catch (e: IOException) {
            Settings()
        }
    }

    override suspend fun save(settings: Settings) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(Settings.serializer(), settings))
    }
}
