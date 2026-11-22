package dev.nitpick.app.git

import dev.nitpick.app.domain.Credentials
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.UnsupportedCredentialItem
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.CredentialsProvider
import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider

/** JGit finds an SSH transport through ServiceLoader: the Apache module on desktop, nothing on Android. */
internal val sshAvailable: Boolean
    get() = SshSessionFactory.getInstance() != null

/** The token for an HTTPS remote; for an SSH remote the machine's keys and agent authenticate. */
internal fun Credentials?.toJGit(remoteUrl: String? = null): CredentialsProvider? =
    if (remoteUrl != null && isSshRemote(remoteUrl)) SshPrompts else this?.let { UsernamePasswordCredentialsProvider(it.username, it.token) }

/**
 * Answers the questions the SSH transport asks: a new host key is accepted and stored, like
 * `StrictHostKeyChecking accept-new`; a passphrase Nitpick cannot type is refused with a
 * sentence instead of a hang.
 */
internal object SshPrompts : CredentialsProvider() {
    override fun isInteractive(): Boolean = false

    override fun supports(vararg items: CredentialItem): Boolean =
        items.all { it is CredentialItem.YesNoType || it is CredentialItem.InformationalMessage }

    override fun get(uri: URIish, vararg items: CredentialItem): Boolean {
        for (item in items) {
            when (item) {
                is CredentialItem.YesNoType -> item.value = true
                is CredentialItem.InformationalMessage -> Unit
                else -> throw UnsupportedCredentialItem(uri, SSH_LOGIN_REFUSED)
            }
        }
        return true
    }
}

internal const val SSH_LOGIN_REFUSED =
    "SSH login refused: load the key in your agent (ssh-add --apple-use-keychain ~/.ssh/<key>) or use a key without passphrase"

/** JGit's "Cannot log in" says nothing about why; over SSH the cause is the key, not the network. */
internal inline fun <T> translatingSshErrors(remoteUrl: String?, block: () -> T): T =
    try {
        block()
    } catch (e: TransportException) {
        val sshLogin = remoteUrl != null && isSshRemote(remoteUrl) && "Cannot log in" in e.message.orEmpty()
        if (sshLogin) throw IllegalStateException(SSH_LOGIN_REFUSED, e) else throw e
    }
