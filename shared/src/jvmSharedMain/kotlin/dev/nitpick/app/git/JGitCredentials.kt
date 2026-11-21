package dev.nitpick.app.git

import dev.nitpick.app.domain.Credentials
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

internal fun Credentials?.toJGit(): CredentialsProvider? =
    this?.let { UsernamePasswordCredentialsProvider(it.username, it.token) }
