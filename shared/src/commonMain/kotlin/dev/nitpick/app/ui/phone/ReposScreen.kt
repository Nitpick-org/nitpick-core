package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Alignment
import dev.nitpick.app.ui.components.NitpickIcon
import nitpick.shared.generated.resources.ic_cloud
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.nitpickAppIcon
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.components.NitpickEmptyState
import dev.nitpick.app.ui.components.NitpickToolbarAction
import dev.nitpick.app.ui.repository.folderName
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.clone_repo
import nitpick.shared.generated.resources.connect_edit
import nitpick.shared.generated.resources.repos_empty_hint
import nitpick.shared.generated.resources.repos_title
import nitpick.shared.generated.resources.repository_empty_title
import org.jetbrains.compose.resources.stringResource

private val RepositoryRowHeight = 32.dp

/** Recent repositories, newest first. */
@Composable
fun ReposScreen(
    recentRepositories: List<String>,
    onOpen: (String) -> Unit,
    onClone: () -> Unit,
    onEditConnection: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    accountLogin: String = "",
    accountIcon: DrawableResource? = null,
) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.repos_title), onBack = null) {
            NitpickToolbarAction(
                text = accountLogin.ifBlank { stringResource(Res.string.connect_edit) },
                onClick = onEditConnection,
                modifier = Modifier.heightIn(min = PhoneTouchTarget),
                leading = accountIcon?.let { { NitpickIcon(it, contentDescription = null, tint = NitpickTheme.colors.onToolbar) } },
            )
            SettingsAction(onOpenSettings)
        }
        if (recentRepositories.isEmpty()) {
            NitpickEmptyState(
                title = stringResource(Res.string.repository_empty_title),
                detail = stringResource(Res.string.repos_empty_hint),
                actionLabel = stringResource(Res.string.clone_repo),
                onAction = onClone,
                modifier = Modifier.weight(1f),
                icon = nitpickAppIcon(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(recentRepositories, key = { it }) { path -> RepositoryCard(path, onClick = { onOpen(path) }) }
            }
            PhoneBottomButton(text = stringResource(Res.string.clone_repo), onClick = onClone, icon = Res.drawable.ic_cloud)
        }
    }
}

@Composable
private fun RepositoryCard(path: String, onClick: () -> Unit) {
    // The storage path is the app's private folder: it tells the user nothing, the name is enough.
    NitpickCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().heightIn(min = RepositoryRowHeight), verticalAlignment = Alignment.CenterVertically) {
            Text(text = folderName(path), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.text, modifier = Modifier.weight(1f))
            Text("›", style = NitpickTheme.typography.title, color = NitpickTheme.colors.textMuted)
        }
    }
}
