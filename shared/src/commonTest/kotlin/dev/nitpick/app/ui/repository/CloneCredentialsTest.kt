package dev.nitpick.app.ui.repository

import dev.nitpick.app.domain.Credentials
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CloneCredentialsTest {

    @Test
    fun aPublicCloneHasNoCredentials() {
        assertNull(cloneCredentials("https://github.com/acme/demo.git", "  "))
    }

    @Test
    fun aGitHubTokenUsesTheGitHubUserName() {
        assertEquals(
            Credentials("x-access-token", "ghp_1"),
            cloneCredentials("https://github.com/acme/demo.git", " ghp_1 "),
        )
    }

    @Test
    fun aGitLabTokenUsesOauth2() {
        assertEquals(
            Credentials("oauth2", "glpat"),
            cloneCredentials("https://gitlab.com/acme/demo.git", "glpat"),
        )
    }
}
