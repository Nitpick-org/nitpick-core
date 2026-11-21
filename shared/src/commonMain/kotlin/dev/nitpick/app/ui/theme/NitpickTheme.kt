package dev.nitpick.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import dev.nitpick.app.settings.ThemeMode
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.jetbrains_mono_medium
import nitpick.shared.generated.resources.jetbrains_mono_regular
import org.jetbrains.compose.resources.Font

private val LocalNitpickColors = staticCompositionLocalOf { NitpickLightColors }
private val LocalNitpickTypography = staticCompositionLocalOf { NitpickTypography() }
private val LocalNitpickSpacing = staticCompositionLocalOf { NitpickSpacing() }
private val LocalNitpickRadii = staticCompositionLocalOf { NitpickRadii() }
private val LocalNitpickSizes = staticCompositionLocalOf { NitpickSizes() }

/**
 * The only theme entry point. It maps the Nitpick tokens onto Material 3 so stock components
 * inherit the same colors, then exposes the extra tokens (lanes, diff, density) through
 * [NitpickTheme].
 *
 * [themeMode] is the persisted user preference; the boolean overload remains available for
 * previews and tests.
 */
@Composable
fun NitpickTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    NitpickTheme(darkTheme = dark, content = content)
}

@Composable
fun NitpickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) NitpickDarkColors else NitpickLightColors
    val codeFamily = FontFamily(
        Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
    )
    // Every one of these values is immutable for a given [darkTheme]. Remembering them keeps
    // their identity stable, so a recomposition of the calling screen does not invalidate
    // every Text through MaterialTheme / the CompositionLocals.
    val typography = remember(codeFamily) { NitpickTypography(codeFamily) }
    val spacing = remember { NitpickSpacing() }
    val radii = remember { NitpickRadii() }
    val sizes = remember { NitpickSizes() }
    val shapes = remember(radii) {
        Shapes(
            extraSmall = RoundedCornerShape(radii.xs),
            small = RoundedCornerShape(radii.sm),
            medium = RoundedCornerShape(radii.md),
            large = RoundedCornerShape(radii.lg),
            extraLarge = RoundedCornerShape(radii.xl),
        )
    }
    val colorScheme = remember(darkTheme) { colors.toMaterialColorScheme(darkTheme) }
    val materialTypography = remember(typography) { typography.toMaterialTypography() }

    CompositionLocalProvider(
        LocalNitpickColors provides colors,
        LocalNitpickTypography provides typography,
        LocalNitpickSpacing provides spacing,
        LocalNitpickRadii provides radii,
        LocalNitpickSizes provides sizes,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = materialTypography,
            shapes = shapes,
            content = content,
        )
    }
}

object NitpickTheme {
    val colors: NitpickColors
        @Composable @ReadOnlyComposable get() = LocalNitpickColors.current

    val typography: NitpickTypography
        @Composable @ReadOnlyComposable get() = LocalNitpickTypography.current

    val spacing: NitpickSpacing
        @Composable @ReadOnlyComposable get() = LocalNitpickSpacing.current

    val radii: NitpickRadii
        @Composable @ReadOnlyComposable get() = LocalNitpickRadii.current

    val sizes: NitpickSizes
        @Composable @ReadOnlyComposable get() = LocalNitpickSizes.current
}

private fun NitpickColors.toMaterialColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primarySoft,
        onPrimaryContainer = text,
        secondary = textSecondary,
        onSecondary = surface,
        secondaryContainer = surfaceSunken,
        onSecondaryContainer = text,
        tertiary = suggestion,
        onTertiary = surface,
        tertiaryContainer = suggestionSoft,
        onTertiaryContainer = text,
        background = canvas,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = surfaceSunken,
        onSurfaceVariant = textSecondary,
        outline = borderStrong,
        outlineVariant = border,
        error = danger,
        onError = Color.White,
        errorContainer = dangerSoft,
        onErrorContainer = text,
        scrim = Color.Black,
        inverseSurface = text,
        inverseOnSurface = canvas,
    )
}
