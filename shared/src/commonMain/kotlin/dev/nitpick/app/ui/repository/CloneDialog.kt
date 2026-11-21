package dev.nitpick.app.ui.repository

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.settings.GitConnection
import dev.nitpick.app.settings.GitHost
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.phone.defaultCloneFolderName
import dev.nitpick.app.ui.phone.isValidCloneFolderName
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.action_cancel
import nitpick.shared.generated.resources.clone_action
import nitpick.shared.generated.resources.clone_choose
import nitpick.shared.generated.resources.clone_destination_label
import nitpick.shared.generated.resources.clone_error_title
import nitpick.shared.generated.resources.clone_folder_label
import nitpick.shared.generated.resources.clone_in_progress
import nitpick.shared.generated.resources.clone_title
import nitpick.shared.generated.resources.clone_token_help
import nitpick.shared.generated.resources.clone_url_label
import nitpick.shared.generated.resources.clone_url_placeholder
import nitpick.shared.generated.resources.connect_token_hide
import nitpick.shared.generated.resources.connect_token_label
import nitpick.shared.generated.resources.connect_token_show
import org.jetbrains.compose.resources.stringResource

private val DialogWidth = 480.dp

/**
 * Desktop clone form, the same fields as the phone Clone screen plus a token and a parent
 * folder picked with [pickDirectory]. It stays open while the clone runs so an error shows in
 * place; [onCloned] fires once the repository is loaded.
 */
@Composable
fun CloneDialog(
    state: RepositoryUiState,
    pickDirectory: () -> String?,
    onClone: (url: String, path: String, credentials: Credentials?) -> Unit,
    onCancel: () -> Unit,
    onCloned: () -> Unit,
) {
    var url by remember { mutableStateOf("") }
    // Plain remember: the token never lands in a saved-state bundle, and dies with the dialog.
    var token by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }
    var parent by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf("") }
    var targetPath by remember { mutableStateOf<String?>(null) }
    val folderName = folder.trim().ifBlank { defaultCloneFolderName(url) }
    val clone = state.clone?.takeIf { it.path == targetPath }
    // The clone is done and its repository replaced the state; loading the log may still fail.
    val isLoaded = targetPath != null && state.clone == null && state.path == targetPath
    val isCloning = (clone != null && clone.errorMessage == null) || (isLoaded && state.isLoading)
    val errorMessage = clone?.errorMessage ?: state.errorMessage.takeIf { isLoaded }

    LaunchedEffect(isLoaded, state.isLoading, state.errorMessage) {
        if (isLoaded && !state.isLoading && state.errorMessage == null) onCloned()
    }

    Dialog(onDismissRequest = { if (!isCloning) onCancel() }) {
        Surface(
            modifier = Modifier.width(DialogWidth),
            shape = RoundedCornerShape(NitpickTheme.radii.xl),
            color = NitpickTheme.colors.surface,
            border = BorderStroke(1.dp, NitpickTheme.colors.borderStrong),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.clone_title), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.text)
                NitpickTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = stringResource(Res.string.clone_url_label),
                    placeholder = stringResource(Res.string.clone_url_placeholder),
                    enabled = !isCloning,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    NitpickTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = stringResource(Res.string.connect_token_label),
                        enabled = !isCloning,
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        trailing = {
                            NitpickButton(
                                text = stringResource(if (showToken) Res.string.connect_token_hide else Res.string.connect_token_show),
                                onClick = { showToken = !showToken },
                                variant = NitpickButtonVariant.Ghost,
                                size = NitpickButtonSize.Small,
                            )
                        },
                    )
                    Text(stringResource(Res.string.clone_token_help), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                    NitpickTextField(
                        value = parent,
                        onValueChange = { parent = it },
                        label = stringResource(Res.string.clone_destination_label),
                        enabled = !isCloning,
                        modifier = Modifier.weight(1f),
                    )
                    NitpickButton(
                        text = stringResource(Res.string.clone_choose),
                        onClick = { pickDirectory()?.let { parent = it } },
                        enabled = !isCloning,
                        variant = NitpickButtonVariant.Secondary,
                    )
                }
                NitpickTextField(
                    value = folderName,
                    onValueChange = { folder = it },
                    label = stringResource(Res.string.clone_folder_label),
                    enabled = !isCloning,
                )
                if (errorMessage != null) {
                    Text(stringResource(Res.string.clone_error_title), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.danger)
                    Text(errorMessage, style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    NitpickButton(stringResource(Res.string.action_cancel), onCancel, variant = NitpickButtonVariant.Secondary, enabled = !isCloning)
                    NitpickButton(
                        text = stringResource(if (isCloning) Res.string.clone_in_progress else Res.string.clone_action),
                        enabled = !isCloning && url.isNotBlank() && parent.isNotBlank() && isValidCloneFolderName(folderName),
                        onClick = {
                            val path = "${parent.trim().trimEnd('/', '\\')}/$folderName"
                            targetPath = path
                            onClone(url.trim(), path, cloneCredentials(url, token))
                        },
                    )
                }
            }
        }
    }
}

/** A blank token clones anonymously; otherwise the forge's token user name is picked from the URL. */
internal fun cloneCredentials(url: String, token: String): Credentials? {
    if (token.isBlank()) return null
    val host = if (url.contains("gitlab", ignoreCase = true)) GitHost.GITLAB else GitHost.GITHUB
    return GitConnection(host, token.trim()).toCredentials()
}
