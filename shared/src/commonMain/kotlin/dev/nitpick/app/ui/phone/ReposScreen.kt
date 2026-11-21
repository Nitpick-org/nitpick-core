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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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

/** Recent repositories, newest first. */
@Composable
fun ReposScreen(
    recentRepositories: List<String>,
    onOpen: (String) -> Unit,
    onClone: () -> Unit,
    onEditConnection: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.repos_title), onBack = null) {
            NitpickToolbarAction(text = stringResource(Res.string.connect_edit), onClick = onEditConnection)
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
            PhoneBottomButton(text = stringResource(Res.string.clone_repo), onClick = onClone)
        }
    }
}

@Composable
private fun RepositoryCard(path: String, onClick: () -> Unit) {
    NitpickCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(text = folderName(path), style = NitpickTheme.typography.bodyStrong, color = NitpickTheme.colors.text)
        Text(
            text = path,
            style = NitpickTheme.typography.codeSmall,
            color = NitpickTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
