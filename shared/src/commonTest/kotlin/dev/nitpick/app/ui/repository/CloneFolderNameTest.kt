package dev.nitpick.app.ui.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloneFolderNameTest {

    @Test
    fun theDefaultFolderIsTheRepositoryNameWithoutDotGit() {
        assertEquals("demo", defaultCloneFolderName("https://github.com/acme/demo.git"))
        assertEquals("demo", defaultCloneFolderName(" https://github.com/acme/demo/ "))
        assertEquals("demo", defaultCloneFolderName("git@github.com:acme/demo.git"))
        assertEquals("", defaultCloneFolderName(""))
    }

    @Test
    fun aFolderNameCannotLeaveTheCloneRoot() {
        assertTrue(isValidCloneFolderName("demo"))
        assertFalse(isValidCloneFolderName(""))
        assertFalse(isValidCloneFolderName(".."))
        assertFalse(isValidCloneFolderName("../etc"))
        assertFalse(isValidCloneFolderName("a\\b"))
    }
}
