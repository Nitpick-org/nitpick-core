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
    override fun create(identityAgent: String?, homeDir: File?): Connector = LenientConnector(delegate.create(identityAgent, homeDir))
}

private class LenientConnector(private val delegate: Connector) : Connector by delegate {
    override fun connect(): Boolean = try {
        delegate.connect()
    } catch (e: IOException) {
        false
    }
}
