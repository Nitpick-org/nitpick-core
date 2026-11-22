package dev.nitpick.app.settings

import kotlin.test.Test
import kotlin.test.assertTrue

class LlmModelsTest {

    @Test
    fun everyProviderHasAtLeastOneKnownModel() {
        LlmProvider.entries.forEach { provider ->
            assertTrue(provider.knownModels.isNotEmpty(), "$provider has no known models")
        }
    }

    @Test
    fun defaultSettingsPointAtAKnownModelOfTheirProvider() {
        val settings = Settings()
        assertTrue(settings.model in settings.provider.knownModels, "${settings.model} is not a known ${settings.provider} model")
    }
}
