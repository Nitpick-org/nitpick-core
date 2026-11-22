package dev.nitpick.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Forces the resource locale at runtime. JetBrains' documented workaround until Compose
 * Multiplatform has a public API: each platform swaps its default locale, and the caller
 * wraps the content in `key(tag)` so every string recomposes. `null` means the system locale.
 */
expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
