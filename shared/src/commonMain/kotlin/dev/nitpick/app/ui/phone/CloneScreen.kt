package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Credentials
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.repository.RepositoryUiState
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.clone_action
import nitpick.shared.generated.resources.clone_error_title
import nitpick.shared.generated.resources.clone_folder_label
import nitpick.shared.generated.resources.clone_help
import nitpick.shared.generated.resources.clone_in_progress
import nitpick.shared.generated.resources.clone_title
import nitpick.shared.generated.resources.clone_token_help
import nitpick.shared.generated.resources.clone_token_hide
import nitpick.shared.generated.resources.clone_token_label
import nitpick.shared.generated.resources.clone_token_show
import nitpick.shared.generated.resources.clone_url_label
import nitpick.shared.generated.resources.clone_url_placeholder
import org.jetbrains.compose.resources.stringResource

// GitHub accepts any user name with a personal access token; this one also fits GitHub App tokens.
// shortcut: GitLab wants "oauth2" for OAuth tokens, add a provider switch when GitLab is tested.
internal const val TOKEN_USERNAME = "x-access-token"

/**
 * Clone form (UI handoff §4). The screen stays visible while the clone runs so a wrong token
 * can be corrected in place; [onCloned] fires once the repository is loaded.
 */
@Composable
fun CloneScreen(
    state: RepositoryUiState,
    cloneRoot: String,
    onClone: (url: String, path: String, credentials: Credentials?) -> Unit,
    onCancel: () -> Unit,
    onCloned: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var url by rememberSaveable { mutableStateOf("") }
    var folder by rememberSaveable { mutableStateOf("") }
    // The token is kept out of rememberSaveable so it never lands in a saved-state bundle.
    var token by remember { mutableStateOf("") }
    var targetPath by rememberSaveable { mutableStateOf<String?>(null) }
    val folderName = folder.trim().ifBlank { defaultCloneFolderName(url) }
    val isOurs = targetPath != null && state.path == targetPath
    val isCloning = isOurs && state.isLoading
    val errorMessage = if (isOurs) state.errorMessage else null

    LaunchedEffect(state.path, state.isLoading, state.errorMessage) {
        if (isOurs && !state.isLoading && state.errorMessage == null) onCloned()
    }

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
            TokenField(token, onValueChange = { token = it }, enabled = !isCloning)
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
                targetPath = path
                onClone(url.trim(), path, token.trim().takeIf { it.isNotEmpty() }?.let { Credentials(TOKEN_USERNAME, it) })
            },
        )
    }
}

@Composable
private fun TokenField(token: String, onValueChange: (String) -> Unit, enabled: Boolean) {
    var visible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitpickTextField(
            value = token,
            onValueChange = onValueChange,
            label = stringResource(Res.string.clone_token_label),
            enabled = enabled,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            // A password keyboard keeps the token out of the keyboard's suggestion dictionary.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailing = {
                Text(
                    text = stringResource(if (visible) Res.string.clone_token_hide else Res.string.clone_token_show),
                    style = NitpickTheme.typography.label,
                    color = NitpickTheme.colors.primary,
                    modifier = Modifier.clickable { visible = !visible }.padding(4.dp),
                )
            },
        )
        Text(stringResource(Res.string.clone_token_help), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textMuted)
    }
}

@Composable
private fun CloneError(message: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(Res.string.clone_error_title), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.danger)
        Text(message, style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
    }
}

/** `https://github.com/acme/demo.git` → `demo`, like `git clone` does. */
internal fun defaultCloneFolderName(url: String): String =
    url.trim().trimEnd('/').substringAfterLast('/').substringAfterLast(':').removeSuffix(".git")

/** The clone must stay inside the clone root, so the name cannot be a path. */
internal fun isValidCloneFolderName(name: String): Boolean =
    name.isNotBlank() && name != "." && name != ".." && name.none { it == '/' || it == '\\' }
