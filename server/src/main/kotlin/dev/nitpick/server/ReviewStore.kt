package dev.nitpick.server

import dev.nitpick.app.domain.SharedReview
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.serialization.json.Json

/**
 * Shared reviews in memory, mirrored to [file] after every share so a restart loses nothing.
 * shortcut: the whole file is rewritten on each share, fine for a demo's few dozen reviews; move to a database past that.
 */
class ReviewStore(private val file: Path) {

    private val lock = Any()
    private var reviews: List<SharedReview> =
        if (file.exists()) Json.decodeFromString(file.readText()) else emptyList()

    /** Stores [review] under a new id and returns it with that id. */
    fun add(review: SharedReview): SharedReview = synchronized(lock) {
        val saved = review.copy(id = UUID.randomUUID().toString(), repoUrl = normalizeRepoUrl(review.repoUrl))
        val updated = reviews + saved
        // Disk first: if the write fails, memory does not claim a review the next start would lose.
        write(updated)
        reviews = updated
        saved
    }

    /** Newest first. */
    fun list(repoUrl: String): List<SharedReview> = synchronized(lock) {
        val key = normalizeRepoUrl(repoUrl)
        reviews.filter { it.repoUrl == key }.reversed()
    }

    fun get(id: String): SharedReview? = synchronized(lock) { reviews.firstOrNull { it.id == id } }

    private fun write(content: List<SharedReview>) {
        file.toAbsolutePath().parent.createDirectories()
        // Write then rename, so a crash mid-write never leaves a half-written file behind.
        val temporary = file.resolveSibling("${file.name}.tmp")
        temporary.writeText(Json.encodeToString(content))
        Files.move(temporary, file, REPLACE_EXISTING, ATOMIC_MOVE)
    }
}

private val SCP_REMOTE = Regex("""[\w.-]+@([^:/]+):(.+)""")
private val SCHEME_REMOTE = Regex("""(?:https?|ssh|git)://(?:[^@/]+@)?([^/:]+)(?::\d+)?/(.+)""")

/**
 * One key per repository whatever the clone used: the laptop's `git@github.com:o/r.git` and the
 * phone's `https://github.com/o/r` both become `https://github.com/o/r`. Anything else is kept as is.
 */
internal fun normalizeRepoUrl(url: String): String {
    val scp = SCP_REMOTE.matchEntire(url)
    val withScheme = if (scp != null) "https://${scp.groupValues[1]}/${scp.groupValues[2]}" else url
    val (host, path) = SCHEME_REMOTE.matchEntire(withScheme)?.destructured ?: return url
    return "https://${host.lowercase()}/${path.trimEnd('/').removeSuffix(".git")}"
}
