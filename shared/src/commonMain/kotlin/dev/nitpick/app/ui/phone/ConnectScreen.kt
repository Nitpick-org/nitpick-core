package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.nitpick.app.settings.GitConnection
import dev.nitpick.app.settings.GitHost
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.connect_action
import nitpick.shared.generated.resources.connect_github
import nitpick.shared.generated.resources.connect_gitlab
import nitpick.shared.generated.resources.connect_help
import nitpick.shared.generated.resources.connect_host
import nitpick.shared.generated.resources.connect_title
import nitpick.shared.generated.resources.connect_token_help
import nitpick.shared.generated.resources.connect_token_hide
import nitpick.shared.generated.resources.connect_token_label
import nitpick.shared.generated.resources.connect_token_show
import org.jetbrains.compose.resources.stringResource

private val HostChoiceHeight = 48.dp

/**
 * The forge account the phone uses for every clone and push. [onCancel] is null the first time:
 * there is nothing to go back to until an account is connected.
 */
@Composable
fun ConnectScreen(
    current: GitConnection?,
    onConnect: (GitConnection) -> Unit,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var host by rememberSaveable { mutableStateOf(current?.host ?: GitHost.GITHUB) }
    // Starts empty even when editing, so the saved token is never shown; blank keeps it. Not in
    // rememberSaveable, so it never lands in a saved-state bundle.
    var token by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.connect_title), onBack = onCancel)
        Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(Res.string.connect_help), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
            Text(stringResource(Res.string.connect_host), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textSecondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HostChoice(stringResource(Res.string.connect_github), selected = host == GitHost.GITHUB, onClick = { host = GitHost.GITHUB }, Modifier.weight(1f))
                HostChoice(stringResource(Res.string.connect_gitlab), selected = host == GitHost.GITLAB, onClick = { host = GitHost.GITLAB }, Modifier.weight(1f))
            }
            NitpickTextField(
                value = token,
                onValueChange = { token = it },
                label = stringResource(Res.string.connect_token_label),
                visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailing = {
                    Text(
                        text = stringResource(if (showToken) Res.string.connect_token_hide else Res.string.connect_token_show),
                        style = NitpickTheme.typography.label,
                        color = NitpickTheme.colors.primary,
                        modifier = Modifier.clickable { showToken = !showToken }.padding(4.dp),
                    )
                },
            )
            Text(stringResource(Res.string.connect_token_help), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textMuted)
        }
        PhoneBottomButton(
            text = stringResource(Res.string.connect_action),
            enabled = token.isNotBlank() || current != null,
            onClick = { onConnect(GitConnection(host, token.trim().ifBlank { current?.token.orEmpty() })) },
        )
    }
}

@Composable
private fun HostChoice(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    NitpickButton(
        text = label,
        onClick = onClick,
        variant = if (selected) NitpickButtonVariant.Primary else NitpickButtonVariant.Secondary,
        size = NitpickButtonSize.Large,
        modifier = modifier.height(HostChoiceHeight),
    )
}
