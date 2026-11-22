package dev.nitpick.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nitpick.app.settings.DEFAULT_SERVER_URL
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.repository.label
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.connect_token_help
import nitpick.shared.generated.resources.connect_token_hide
import nitpick.shared.generated.resources.connect_token_label
import nitpick.shared.generated.resources.connect_token_show
import nitpick.shared.generated.resources.ic_anthropic
import nitpick.shared.generated.resources.ic_google
import nitpick.shared.generated.resources.ic_ollama
import nitpick.shared.generated.resources.ic_openai
import nitpick.shared.generated.resources.settings_api_key
import nitpick.shared.generated.resources.settings_api_key_help
import nitpick.shared.generated.resources.settings_server_url
import nitpick.shared.generated.resources.settings_server_url_help
import nitpick.shared.generated.resources.settings_server_token_help
import nitpick.shared.generated.resources.settings_server_token
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource

/** Model select shared by the desktop dialog and the phone screen: a secondary button plus a menu. */
@Composable
internal fun ModelSelector(model: String, models: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier, touch: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        NitpickButton(
            text = "$model \u25BE",
            onClick = { expanded = true },
            variant = NitpickButtonVariant.Secondary,
            size = if (touch) NitpickButtonSize.Large else NitpickButtonSize.Small,
            modifier = Modifier.fillMaxSize(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            models.forEach { candidate ->
                DropdownMenuItem(
                    text = { Text(candidate, style = NitpickTheme.typography.codeSmall) },
                    onClick = { expanded = false; onSelect(candidate) },
                )
            }
        }
    }
}

/** Bearer token for a LAN review server (`NITPICK_TOKEN`); empty on localhost. */
@Composable
internal fun ServerTokenField(serverToken: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, minHeight: Dp = 34.dp) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitpickTextField(
            value = serverToken,
            onValueChange = onChange,
            label = stringResource(Res.string.settings_server_token),
            placeholder = "",
            minHeight = minHeight,
        )
        Text(
            text = stringResource(Res.string.settings_server_token_help),
            style = NitpickTheme.typography.micro,
            color = NitpickTheme.colors.textMuted,
        )
    }
}

/** URL of the review-sharing server, with the help line on how a phone reaches the laptop. */
@Composable
internal fun ServerUrlField(serverUrl: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, minHeight: Dp = 34.dp) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitpickTextField(
            value = serverUrl,
            onValueChange = onChange,
            label = stringResource(Res.string.settings_server_url),
            placeholder = DEFAULT_SERVER_URL,
            minHeight = minHeight,
        )
        Text(
            text = stringResource(Res.string.settings_server_url_help),
            style = NitpickTheme.typography.micro,
            color = NitpickTheme.colors.textMuted,
        )
    }
}

/** Masked forge token with Show/Hide and the read/write help line. */
@Composable
internal fun GitTokenField(
    token: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 34.dp,
) {
    var showToken by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitpickTextField(
            value = token,
            onValueChange = onChange,
            label = stringResource(Res.string.connect_token_label),
            visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
            minHeight = minHeight,
            trailing = {
                NitpickButton(
                    text = stringResource(if (showToken) Res.string.connect_token_hide else Res.string.connect_token_show),
                    onClick = { showToken = !showToken },
                    variant = NitpickButtonVariant.Ghost,
                    size = NitpickButtonSize.Small,
                )
            },
        )
        Text(
            text = stringResource(Res.string.connect_token_help),
            style = NitpickTheme.typography.micro,
            color = NitpickTheme.colors.textMuted,
        )
    }
}

internal val LlmProvider.icon: DrawableResource
    get() = when (this) {
        LlmProvider.ANTHROPIC -> Res.drawable.ic_anthropic
        LlmProvider.OPENAI -> Res.drawable.ic_openai
        LlmProvider.GOOGLE -> Res.drawable.ic_google
        LlmProvider.OLLAMA -> Res.drawable.ic_ollama
    }

/** Masked key field with Show/Hide and the help line naming the provider the key goes to. */
@Composable
internal fun ApiKeyField(
    apiKey: String,
    provider: LlmProvider,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 34.dp,
) {
    var showKey by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitpickTextField(
            value = apiKey,
            onValueChange = onChange,
            label = stringResource(Res.string.settings_api_key),
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            minHeight = minHeight,
            trailing = {
                NitpickButton(
                    text = stringResource(if (showKey) Res.string.connect_token_hide else Res.string.connect_token_show),
                    onClick = { showKey = !showKey },
                    variant = NitpickButtonVariant.Ghost,
                    size = NitpickButtonSize.Small,
                )
            },
        )
        Text(
            text = stringResource(Res.string.settings_api_key_help, stringResource(provider.label)),
            style = NitpickTheme.typography.micro,
            color = NitpickTheme.colors.textMuted,
        )
    }
}
