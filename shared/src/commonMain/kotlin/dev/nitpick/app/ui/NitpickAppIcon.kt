package dev.nitpick.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.ic_app_dark
import nitpick.shared.generated.resources.ic_app_light
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** The tweezers app icon (desktop window, welcome screens) in the variant of the current theme. */
@Composable
fun nitpickAppIcon(): Painter = painterResource(appIconResource(NitpickTheme.colors.isDark))

internal fun appIconResource(isDark: Boolean): DrawableResource =
    if (isDark) Res.drawable.ic_app_dark else Res.drawable.ic_app_light
