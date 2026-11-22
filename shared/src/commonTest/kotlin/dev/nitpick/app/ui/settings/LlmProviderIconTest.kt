package dev.nitpick.app.ui.settings

import dev.nitpick.app.settings.LlmProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class LlmProviderIconTest {
    @Test
    fun everyProviderHasItsOwnMark() {
        val icons = LlmProvider.entries.map { it.icon }
        assertEquals(icons.size, icons.toSet().size)
    }
}
