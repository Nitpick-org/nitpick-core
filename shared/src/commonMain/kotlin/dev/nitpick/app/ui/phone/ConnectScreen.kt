package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import dev.nitpick.app.ui.components.NitpickIcon
import nitpick.shared.generated.resources.ic_github
import nitpick.shared.generated.resources.ic_gitlab
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.nitpick.app.settings.DeviceCode
import dev.nitpick.app.settings.ForgeClient
import dev.nitpick.app.settings.DeviceFlowException
import dev.nitpick.app.settings.ForgeOAuthClientIds
import dev.nitpick.app.settings.GitConnection
import dev.nitpick.app.settings.GitHost
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonSize
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.connect_action
import nitpick.shared.generated.resources.connect_current
import nitpick.shared.generated.resources.connect_github
import nitpick.shared.generated.resources.connect_gitlab
import nitpick.shared.generated.resources.connect_help
import nitpick.shared.generated.resources.connect_host
import nitpick.shared.generated.resources.connect_signin
import nitpick.shared.generated.resources.connect_signin_cancel
import nitpick.shared.generated.resources.connect_signin_code_help
import nitpick.shared.generated.resources.connect_signin_denied
import nitpick.shared.generated.resources.connect_signin_expired
import nitpick.shared.generated.resources.connect_signin_failed
import nitpick.shared.generated.resources.connect_signin_open
import nitpick.shared.generated.resources.connect_title
import nitpick.shared.generated.resources.connect_token_or
import nitpick.shared.generated.resources.connect_token_help
import nitpick.shared.generated.resources.connect_token_hide
import nitpick.shared.generated.resources.connect_token_label
import nitpick.shared.generated.resources.connect_token_show
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private val HostChoiceHeight = 48.dp

/** A sign-in waiting for the user to approve in the browser; dropped when they cancel. */
private class PendingSignIn(val host: GitHost, val clientId: String, val code: DeviceCode)

/**
 * The forge account the phone uses for every clone and push. [onCancel] is null the first time:
 * there is nothing to go back to until an account is connected.
 */
@Composable
fun ConnectScreen(
    current: GitConnection?,
    forge: ForgeClient,
    onConnect: (GitConnection) -> Unit,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var host by rememberSaveable { mutableStateOf(current?.host ?: GitHost.GITHUB) }
    // Starts empty even when editing, so the saved token is never shown; blank keeps it. Not in
    // rememberSaveable, so it never lands in a saved-state bundle; same for the sign-in codes.
    var token by remember { mutableStateOf("") }
    var showToken by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingSignIn?>(null) }
    var startingSignIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val clientId = ForgeOAuthClientIds.forHost(host)
    val hostLabel = stringResource(if (host == GitHost.GITLAB) Res.string.connect_gitlab else Res.string.connect_github)
    // No browser on the device: the code stays on screen, the user can open the page elsewhere.
    fun openBrowser(url: String) = try {
        uriHandler.openUri(url)
        signInError = null
    } catch (e: IllegalArgumentException) {
        signInError = e.message
    }

    LaunchedEffect(pending) {
        val signIn = pending ?: return@LaunchedEffect
        try {
            val obtained = forge.awaitToken(signIn.host, signIn.clientId, signIn.code)
            onConnect(GitConnection(signIn.host, obtained, forge.loginOrBlank(signIn.host, obtained)))
        } catch (e: DeviceFlowException) {
            signInError = e.message
        }
        pending = null
    }

    pending?.let { signIn ->
        SignInWaiting(
            hostLabel = hostLabel,
            userCode = signIn.code.userCode,
            error = signInError,
            onOpenBrowser = { openBrowser(signIn.code.verificationUri) },
            onCancel = { pending = null },
            modifier = modifier,
        )
        return
    }

    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.connect_title), onBack = onCancel)
        Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(Res.string.connect_help), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
            current?.takeIf { it.login.isNotBlank() }?.let {
                val currentHost = stringResource(if (it.host == GitHost.GITLAB) Res.string.connect_gitlab else Res.string.connect_github)
                Text(stringResource(Res.string.connect_current, currentHost, it.login), style = NitpickTheme.typography.small, color = NitpickTheme.colors.text)
            }
            Text(stringResource(Res.string.connect_host), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textSecondary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HostChoice(GitHost.GITHUB, selected = host == GitHost.GITHUB, onClick = { host = GitHost.GITHUB }, Modifier.weight(1f))
                HostChoice(GitHost.GITLAB, selected = host == GitHost.GITLAB, onClick = { host = GitHost.GITLAB }, Modifier.weight(1f))
            }
            if (clientId.isNotBlank()) {
                NitpickButton(
                    text = stringResource(Res.string.connect_signin, hostLabel),
                    enabled = !startingSignIn,
                    size = NitpickButtonSize.Large,
                    modifier = Modifier.fillMaxWidth().height(HostChoiceHeight),
                    leading = { NitpickIcon(host.icon, contentDescription = null, tint = NitpickTheme.colors.onPrimary) },
                    onClick = {
                        scope.launch {
                            startingSignIn = true
                            signInError = null
                            try {
                                val code = forge.start(host, clientId)
                                openBrowser(code.verificationUri)
                                pending = PendingSignIn(host, clientId, code)
                            } catch (e: DeviceFlowException) {
                                signInError = e.message
                            } finally {
                                startingSignIn = false
                            }
                        }
                    },
                )
                signInError?.let {
                    Text(signInErrorText(it), style = NitpickTheme.typography.small, color = NitpickTheme.colors.danger)
                }
                Text(stringResource(Res.string.connect_token_or), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textSecondary)
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
                        modifier = Modifier.clickable { showToken = !showToken }.heightIn(min = PhoneTouchTarget).wrapContentHeight().padding(horizontal = 4.dp),
                    )
                },
            )
            Text(stringResource(Res.string.connect_token_help), style = NitpickTheme.typography.micro, color = NitpickTheme.colors.textMuted)
        }
        PhoneBottomButton(
            text = stringResource(Res.string.connect_action),
            enabled = (token.isNotBlank() || current != null) && !startingSignIn,
            onClick = {
                val typed = token.trim()
                scope.launch {
                    startingSignIn = true
                    val kept = typed.ifBlank { current?.token.orEmpty() }
                    val login = if (typed.isBlank() && current?.host == host) current.login else forge.loginOrBlank(host, kept)
                    onConnect(GitConnection(host, kept, login))
                    startingSignIn = false
                }
            },
        )
    }
}

// Not knowing the name must not block the connection: the token still works.
private suspend fun ForgeClient.loginOrBlank(host: GitHost, token: String): String =
    try {
        fetchLogin(host, token)
    } catch (e: DeviceFlowException) {
        ""
    }

@Composable
private fun signInErrorText(code: String): String = when (code) {
    "access_denied" -> stringResource(Res.string.connect_signin_denied)
    "expired_token" -> stringResource(Res.string.connect_signin_expired)
    else -> stringResource(Res.string.connect_signin_failed, code)
}

@Composable
private fun SignInWaiting(hostLabel: String, userCode: String, error: String?, onOpenBrowser: () -> Unit, onCancel: () -> Unit, modifier: Modifier) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.connect_title), onBack = onCancel)
        Column(
            Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(userCode, style = NitpickTheme.typography.display, color = NitpickTheme.colors.text)
            Text(stringResource(Res.string.connect_signin_code_help, hostLabel), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
            error?.let {
                Text(signInErrorText(it), style = NitpickTheme.typography.small, color = NitpickTheme.colors.danger)
            }
            NitpickButton(
                text = stringResource(Res.string.connect_signin_open),
                onClick = onOpenBrowser,
                variant = NitpickButtonVariant.Secondary,
                size = NitpickButtonSize.Large,
                modifier = Modifier.fillMaxWidth().height(HostChoiceHeight),
            )
        }
        PhoneBottomButton(text = stringResource(Res.string.connect_signin_cancel), onClick = onCancel)
    }
}

@Composable
private fun HostChoice(choice: GitHost, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    NitpickButton(
        text = stringResource(if (choice == GitHost.GITLAB) Res.string.connect_gitlab else Res.string.connect_github),
        onClick = onClick,
        variant = if (selected) NitpickButtonVariant.Primary else NitpickButtonVariant.Secondary,
        size = NitpickButtonSize.Large,
        modifier = modifier.height(HostChoiceHeight),
        leading = { NitpickIcon(choice.icon, contentDescription = null, tint = if (selected) NitpickTheme.colors.onPrimary else NitpickTheme.colors.text) },
    )
}

/** The forge's mark (Simple Icons, CC0), tinted like the text next to it. */
internal val GitHost.icon: DrawableResource
    get() = when (this) {
        GitHost.GITHUB -> Res.drawable.ic_github
        GitHost.GITLAB -> Res.drawable.ic_gitlab
    }
