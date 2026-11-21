package dev.nitpick.app.git

import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.domain.PullResult
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.TrackingStatus
import dev.nitpick.app.domain.WorkingTreeStatus

interface GitRepository {
    suspend fun log(limit: Int = 500): List<Commit>
    suspend fun refs(): List<Ref>
    suspend fun currentBranch(): String
    /** URL of the `origin` remote, or null when the repository has none. */
    suspend fun remoteUrl(): String?
    suspend fun status(): WorkingTreeStatus
    suspend fun diffAgainst(baseRef: String): List<FileDiff>
    suspend fun readFile(path: String): String?
    suspend fun listFiles(): List<String>
    suspend fun writeFile(path: String, content: String)
    suspend fun stage(paths: List<String>)
    suspend fun commit(message: String)
    suspend fun fetch(credentials: Credentials?)
    suspend fun pull(credentials: Credentials?): PullResult
    suspend fun push(credentials: Credentials?)
    suspend fun checkout(branch: String)

    /** Ahead/behind of the current branch vs its upstream; null when there is no upstream. */
    suspend fun trackingStatus(): TrackingStatus? = null
}
