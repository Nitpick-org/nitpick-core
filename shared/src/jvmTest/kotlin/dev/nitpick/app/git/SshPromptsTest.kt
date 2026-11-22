package dev.nitpick.app.git

import dev.nitpick.app.domain.Credentials
import org.eclipse.jgit.errors.UnsupportedCredentialItem
import org.eclipse.jgit.transport.CredentialItem
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SshPromptsTest {
    private val uri = URIish("ssh://git@example.com/o/r.git")

    @Test
    fun aNewHostKeyIsAcceptedAndStored() {
        val accept = CredentialItem.YesNoType("Accept and store this key?")
        val note = CredentialItem.InformationalMessage("fingerprint SHA256:…")

        assertTrue(SshPrompts.supports(accept, note))
        assertTrue(SshPrompts.get(uri, accept, note))
        assertTrue(accept.value)
    }

    @Test
    fun aPassphraseIsRefusedWithASentence() {
        val passphrase = CredentialItem.Password("Passphrase for ~/.ssh/id_ed25519")

        assertTrue(!SshPrompts.supports(passphrase))
        val error = assertFailsWith<UnsupportedCredentialItem> { SshPrompts.get(uri, passphrase) }
        assertTrue("agent" in error.message.orEmpty())
    }

    @Test
    fun aRefusedSshLoginGetsASentenceAndOtherTransportErrorsStay() {
        val refused = org.eclipse.jgit.api.errors.TransportException("git@github.com:o/r.git: Cannot log in at github.com:22")
        val network = org.eclipse.jgit.api.errors.TransportException("git@github.com:o/r.git: Connection refused")

        val error = assertFailsWith<IllegalStateException> { translatingSshErrors("git@github.com:o/r.git") { throw refused } }
        assertEquals(SSH_LOGIN_REFUSED, error.message)
        assertFailsWith<org.eclipse.jgit.api.errors.TransportException> { translatingSshErrors("git@github.com:o/r.git") { throw network } }
        assertFailsWith<org.eclipse.jgit.api.errors.TransportException> { translatingSshErrors("https://github.com/o/r.git") { throw refused } }
    }

    @Test
    fun theTokenGoesToHttpsRemotesOnlyAndSshRemotesGetThePrompts() {
        val token = Credentials("thomas", "ghp_x")

        assertIs<UsernamePasswordCredentialsProvider>(token.toJGit("https://github.com/o/r.git"))
        assertEquals(SshPrompts, token.toJGit("git@github.com:o/r.git"))
        assertEquals(SshPrompts, (null as Credentials?).toJGit("ssh://git@github.com/o/r.git"))
        assertNull((null as Credentials?).toJGit("https://github.com/o/r.git"))
    }
}
