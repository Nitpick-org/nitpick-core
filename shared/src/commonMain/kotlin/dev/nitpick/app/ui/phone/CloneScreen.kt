package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.repository.RepositoryUiState
import dev.nitpick.app.ui.repository.defaultCloneFolderName
import dev.nitpick.app.ui.repository.isValidCloneFolderName
import dev.nitpick.app.ui.repository.rememberCloneProgress
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.clone_action
import nitpick.shared.generated.resources.clone_error_title
import nitpick.shared.generated.resources.clone_folder_label
import nitpick.shared.generated.resources.clone_help
import nitpick.shared.generated.resources.clone_in_progress
import nitpick.shared.generated.resources.clone_title
import nitpick.shared.generated.resources.clone_url_label
import nitpick.shared.generated.resources.clone_url_placeholder
import org.jetbrains.compose.resources.stringResource

/**
 * Clone form (UI handoff §4). The screen stays visible while the clone runs so a wrong URL
 * can be corrected in place; [onCloned] fires once the repository is loaded.
 */
@Composable
fun CloneScreen(
    state: RepositoryUiState,
    cloneRoot: String,
    onClone: (url: String, path: String) -> Unit,
    onCancel: () -> Unit,
    onCloned: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var url by rememberSaveable { mutableStateOf("") }
    var folder by rememberSaveable { mutableStateOf("") }
    val folderName = folder.trim().ifBlank { defaultCloneFolderName(url) }
    val progress = rememberCloneProgress(state, onCloned)
    val isCloning = progress.isCloning
    val errorMessage = progress.errorMessage

    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.clone_title), onBack = onCancel)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.clone_help), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
            NitpickTextField(
                value = url,
                onValueChange = { url = it },
                label = stringResource(Res.string.clone_url_label),
                placeholder = stringResource(Res.string.clone_url_placeholder),
                enabled = !isCloning,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            NitpickTextField(
                value = folderName,
                onValueChange = { folder = it },
                label = stringResource(Res.string.clone_folder_label),
                enabled = !isCloning,
            )
            if (errorMessage != null) CloneError(errorMessage)
        }
        PhoneBottomButton(
            text = stringResource(if (isCloning) Res.string.clone_in_progress else Res.string.clone_action),
            enabled = !isCloning && url.isNotBlank() && isValidCloneFolderName(folderName),
            onClick = {
                val path = "$cloneRoot/$folderName"
                progress.start(path)
                onClone(url.trim(), path)
            },
        )
    }
}

@Composable
private fun CloneError(message: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(Res.string.clone_error_title), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.danger)
        Text(message, style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
    }
}
