package dev.nitpick.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.nitpick.app.settings.DeviceCode
import dev.nitpick.app.settings.DeviceFlowException
import dev.nitpick.app.settings.ForgeClient
import dev.nitpick.app.settings.ForgeOAuthClientIds
import dev.nitpick.app.settings.GitConnection
import dev.nitpick.app.settings.GitHost
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickIcon
import dev.nitpick.app.ui.phone.icon
import dev.nitpick.app.ui.phone.loginOrBlank
import dev.nitpick.app.ui.phone.signInErrorText
import dev.nitpick.app.ui.theme.NitpickTheme
import kotlinx.coroutines.launch
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.connect_github
import nitpick.shared.generated.resources.connect_gitlab
import nitpick.shared.generated.resources.connect_signin
import nitpick.shared.generated.resources.connect_signin_cancel
import nitpick.shared.generated.resources.connect_signin_code_help
import nitpick.shared.generated.resources.connect_signin_open
import nitpick.shared.generated.resources.connect_token_or
import org.jetbrains.compose.resources.stringResource

/** A sign-in waiting for the user to approve in the browser; dropped when they cancel. */
private class PendingSignIn(val host: GitHost, val clientId: String, val code: DeviceCode)

/**
 * The phone's device-flow sign-in, as a block for the desktop settings: the button, then the
 * code to type with "Open browser" and "Cancel" until the forge hands the token to [onConnected].
 * Nothing when the forge has no OAuth client id: the token field below stays the only way.
 */
@Composable
internal fun ForgeSignIn(host: GitHost, forge: ForgeClient, onConnected: (GitConnection) -> Unit, modifier: Modifier = Modifier) {
    val clientId = ForgeOAuthClientIds.forHost(host)
    if (clientId.isBlank()) return
    val colors = NitpickTheme.colors
    // Not in rememberSaveable: a sign-in code never lands in a saved-state bundle.
    var pending by remember { mutableStateOf<PendingSignIn?>(null) }
    var starting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val hostLabel = stringResource(if (host == GitHost.GITLAB) Res.string.connect_gitlab else Res.string.connect_github)
    fun openBrowser(url: String) = try {
        uriHandler.openUri(url)
        error = null
    } catch (e: IllegalArgumentException) {
        error = e.message
    }

    LaunchedEffect(pending) {
        val signIn = pending ?: return@LaunchedEffect
        try {
            val token = forge.awaitToken(signIn.host, signIn.clientId, signIn.code)
            onConnected(GitConnection(signIn.host, token, forge.loginOrBlank(signIn.host, token)))
        } catch (e: DeviceFlowException) {
            error = e.message
        }
        pending = null
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val signIn = pending
        if (signIn == null) {
            NitpickButton(
                text = stringResource(Res.string.connect_signin, hostLabel),
                enabled = !starting,
                leading = { NitpickIcon(host.icon, contentDescription = null, tint = colors.onPrimary) },
                onClick = {
                    scope.launch {
                        starting = true
                        error = null
                        try {
                            val code = forge.start(host, clientId)
                            openBrowser(code.verificationUri)
                            pending = PendingSignIn(host, clientId, code)
                        } catch (e: DeviceFlowException) {
                            error = e.message
                        } finally {
                            starting = false
                        }
                    }
                },
            )
        } else {
            Text(signIn.code.userCode, style = NitpickTheme.typography.display, color = colors.text)
            Text(stringResource(Res.string.connect_signin_code_help, hostLabel), style = NitpickTheme.typography.small, color = colors.textSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NitpickButton(
                    text = stringResource(Res.string.connect_signin_open),
                    onClick = { openBrowser(signIn.code.verificationUri) },
                    variant = NitpickButtonVariant.Secondary,
                )
                NitpickButton(
                    text = stringResource(Res.string.connect_signin_cancel),
                    onClick = { pending = null },
                    variant = NitpickButtonVariant.Ghost,
                )
            }
        }
        error?.let { Text(signInErrorText(it), style = NitpickTheme.typography.small, color = colors.danger) }
        Text(stringResource(Res.string.connect_token_or), style = NitpickTheme.typography.micro, color = colors.textSecondary)
    }
}
