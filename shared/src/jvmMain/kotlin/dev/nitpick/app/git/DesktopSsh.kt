package dev.nitpick.app.git

import org.eclipse.jgit.transport.SshSessionFactory
import org.eclipse.jgit.transport.sshd.JGitKeyCache
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder
import org.eclipse.jgit.transport.sshd.agent.Connector
import org.eclipse.jgit.transport.sshd.agent.ConnectorFactory
import org.eclipse.jgit.util.FS
import java.io.File
import java.io.IOException

/**
 * The desktop SSH transport: JGit's Apache module with one difference from its default. An
 * agent that cannot be reached is skipped, as OpenSSH does, instead of failing the whole
 * authentication: a stale `IdentityAgent` in `~/.ssh/config` falls back to the key files.
 */
object DesktopSsh {
    fun install() {
        val home = FS.DETECTED.userHome()
        val factory = SshdSessionFactoryBuilder()
            .setHomeDirectory(home)
            .setSshDirectory(File(home, ".ssh"))
            .setConnectorFactory(ConnectorFactory.getDefault()?.let(::LenientConnectorFactory))
            .build(JGitKeyCache())
        SshSessionFactory.setInstance(factory)
    }
}

private class LenientConnectorFactory(private val delegate: ConnectorFactory) : ConnectorFactory by delegate {
    override fun create(identityAgent: String?, homeDir: File?): Connector = LenientConnector(delegate, identityAgent, homeDir)
}

/**
 * Tries the agent named by `IdentityAgent`, then the default one (`SSH_AUTH_SOCK`), then gives
 * up without an error: the key files are the next step, as with OpenSSH.
 */
private class LenientConnector(
    private val factory: ConnectorFactory,
    private val identityAgent: String?,
    private val homeDir: File?,
) : Connector {
    private var connected: Connector? = null

    override fun connect(): Boolean {
        val candidates = if (identityAgent == null) listOf(null) else listOf(identityAgent, null)
        for (agent in candidates) {
            val connector = try {
                factory.create(agent, homeDir).takeIf { it.connect() }
            } catch (e: IOException) {
                null
            }
            if (connector != null) {
                connected = connector
                return true
            }
        }
        return false
    }

    override fun rpc(command: Byte, message: ByteArray): ByteArray =
        checkNotNull(connected) { "SSH agent not connected" }.rpc(command, message)

    override fun close() {
        connected?.close()
        connected = null
    }
}
