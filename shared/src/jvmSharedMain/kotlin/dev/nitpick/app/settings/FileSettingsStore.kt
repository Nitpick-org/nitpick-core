package dev.nitpick.app.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileSettingsStore(private val file: File) : SettingsStore {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    // One thread, so saves fired by fast typing land on disk in order and never interleave.
    private val io = Dispatchers.IO.limitedParallelism(1)

    override suspend fun load(): Settings = withContext(io) {
        if (!file.isFile) return@withContext Settings()
        try {
            val settings = json.decodeFromString(Settings.serializer(), file.readText())
            if (settings.model.isEmpty()) settings.copy(model = settings.provider.knownModels.first()) else settings
        } catch (e: SerializationException) {
            Settings()
        } catch (e: IOException) {
            Settings()
        }
    }

    override suspend fun save(settings: Settings) = withContext(io) {
        file.parentFile?.mkdirs()
        val tmp = File(file.path + ".tmp")
        tmp.writeText(json.encodeToString(Settings.serializer(), settings))
        tmp.setReadable(false, false)
        tmp.setReadable(true, true)
        try {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        Unit
    }
}
