package dev.nitpick.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.nitpick.app.settings.ThemeMode

private val AccentLight = Color(0xFF0F766E)
private val AccentDark = Color(0xFF5EEAD4)

private val LightColors = lightColorScheme(
    primary = AccentLight,
    secondary = AccentLight,
    tertiary = AccentLight,
)

private val DarkColors = darkColorScheme(
    primary = AccentDark,
    secondary = AccentDark,
    tertiary = AccentDark,
)

@Composable
fun NitpickTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
