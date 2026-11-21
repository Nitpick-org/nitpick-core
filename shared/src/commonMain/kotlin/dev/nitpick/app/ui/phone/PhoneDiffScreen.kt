package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nitpick.app.domain.FileDiff
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.repository.FileHunks
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.diff_empty
import org.jetbrains.compose.resources.stringResource

/** One changed file on a phone, with the same hunk rendering as the desktop inspector. */
@Composable
fun PhoneDiffScreen(file: FileDiff?, baseRef: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.editor)) {
        PhoneTopBar(title = file?.path?.substringAfterLast('/') ?: "", onBack = onBack)
        if (file == null) {
            NitpickEmptyState(title = stringResource(Res.string.diff_empty, baseRef), actionLabel = null, onAction = null)
        } else {
            FileHunks(file, Modifier.fillMaxSize())
        }
    }
}
