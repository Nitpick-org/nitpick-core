package dev.nitpick.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.ic_app
import org.jetbrains.compose.resources.painterResource

/** The loupe app icon (desktop window, welcome screens); the shared module owns the resource. */
@Composable
fun nitpickAppIcon(): Painter = painterResource(Res.drawable.ic_app)
