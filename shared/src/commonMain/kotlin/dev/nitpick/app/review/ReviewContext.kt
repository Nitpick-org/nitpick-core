package dev.nitpick.app.review

import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.git.GitRepository

private const val MAX_FILES = 20
const val MAX_PROMPT_FILE_CHARS = 20_000
const val MAX_PROMPT_TOTAL_CHARS = 120_000
private const val MAX_README_CHARS = 2_000
private const val MAX_TREE_ENTRIES = 200
private const val RECENT_COMMITS = 10

private val IGNORED_FILE_NAMES = setOf(
    "package-lock.json", "yarn.lock", "pnpm-lock.yaml", "bun.lockb", "Cargo.lock", "Gemfile.lock",
    "poetry.lock", "composer.lock", "gradle.lockfile", "go.sum", "Podfile.lock",
    // Secrets: they must never leave the machine, even if a fake repo lists them as tracked.
    ".env", ".env.local", "id_rsa", "id_ed25519", "local.properties",
)
private val IGNORED_EXTENSIONS = setOf(
    "min.js", "min.css", "map", "pb.go", "g.dart", "generated.kt",
    // Asset sidecars and media: a Godot `.import` can be larger than the code it points at.
    "import", "png", "jpg", "jpeg", "webp", "gif", "svg", "ico",
    "ttf", "otf", "woff", "woff2", "mp3", "wav", "ogg", "mp4", "mov",
    "glb", "gltf", "fbx", "blend", "psd", "aseprite",
    "pem", "key", "p12", "jks",
)
private val IGNORED_DIRECTORIES = setOf("build", "dist", "node_modules", ".gradle", "generated", "vendor")

private val LANGUAGE_BY_EXTENSION = mapOf(
    "kt" to "Kotlin", "kts" to "Kotlin", "java" to "Java", "ts" to "TypeScript", "tsx" to "TypeScript",
    "js" to "JavaScript", "jsx" to "JavaScript", "py" to "Python", "go" to "Go", "rs" to "Rust",
    "swift" to "Swift", "rb" to "Ruby", "php" to "PHP", "cs" to "C#", "c" to "C", "cpp" to "C++",
)

/** [files] holds every readable changed file: anchoring needs all of them, the prompt prints only the small ones. */
data class ReviewContext(
    val baseRef: String,
    val branch: String,
    val diffs: List<FileDiff>,
    val files: Map<String, String>,
    val fileTree: List<String>,
    val readme: String?,
    val recentCommits: List<String>,
    val language: String?,
)

/** Keeps the diff the model can use: lockfiles, binaries and generated files are noise. */
fun isReviewable(diff: FileDiff): Boolean = isReviewablePath(diff.path, diff.isBinary)

/** Same filter for a path the review agent asks to read; binaries and hidden secrets stay out. */
fun isReviewablePath(path: String, isBinary: Boolean = false): Boolean {
    val name = path.substringAfterLast('/')
    val segments = path.split('/')
    return !isBinary &&
        name !in IGNORED_FILE_NAMES &&
        !name.startsWith(".env.") &&
        IGNORED_EXTENSIONS.none { name.endsWith(".$it") } &&
        segments.dropLast(1).none { it in IGNORED_DIRECTORIES }
}

fun detectLanguage(paths: List<String>): String? =
    paths.mapNotNull { LANGUAGE_BY_EXTENSION[it.substringAfterLast('.', "")] }
        .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

/** Largest changes first: the prompt budget should go to code, not to the first files alphabetically. */
internal fun selectReviewDiffs(diffs: List<FileDiff>, limit: Int = MAX_FILES): List<FileDiff> =
    diffs
        .filter(::isReviewable)
        .sortedWith(compareByDescending<FileDiff> { changedLineCount(it) }.thenBy { it.path })
        .take(limit)

private fun changedLineCount(diff: FileDiff): Int = diff.addedLines + diff.removedLines

suspend fun buildReviewContext(repository: GitRepository, baseRef: String): ReviewContext {
    val diffs = selectReviewDiffs(repository.diffAgainst(baseRef))
    val files = diffs
        .filter { it.hunks.isNotEmpty() }
        .mapNotNull { diff -> repository.readFile(diff.path)?.let { diff.path to it } }
        .toMap()
    val tree = repository.listFiles()
    val readme = tree.firstOrNull { it.equals("README.md", ignoreCase = true) }
        ?.let { repository.readFile(it) }?.take(MAX_README_CHARS)
    return ReviewContext(
        baseRef = baseRef,
        branch = repository.currentBranch(),
        diffs = diffs,
        files = files,
        fileTree = tree.take(MAX_TREE_ENTRIES),
        readme = readme,
        recentCommits = repository.log(RECENT_COMMITS).map { it.shortMessage },
        language = detectLanguage(diffs.map { it.path }),
    )
}
