package dev.nitpick.app.git

import dev.nitpick.app.domain.Credentials

interface GitRepositoryFactory {
    suspend fun open(path: String): GitRepository
    suspend fun clone(url: String, path: String, credentials: Credentials?): GitRepository
}
