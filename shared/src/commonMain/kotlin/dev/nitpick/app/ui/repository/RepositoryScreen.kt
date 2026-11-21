package dev.nitpick.app.ui.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.Commit
import dev.nitpick.app.domain.Ref
import dev.nitpick.app.domain.RefType
import nitpick.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Desktop layout from docs/CONTEXT.md §12: refs sidebar | commit list | details. The details
 * panel stays a placeholder until the diff and review tickets land.
 *
 * [onOpenRepository] is null on platforms without a folder picker.
 */
@Composable
fun RepositoryScreen(
    state: RepositoryUiState,
    onOpenRepository: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        if (state.path != null) {
            RepositoryHeader(state.path, state.currentBranch, onOpenRepository)
            HorizontalDivider()
        }
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.errorMessage != null -> MessageState(
                title = stringResource(Res.string.repository_error_title),
                detail = state.errorMessage,
                onOpenRepository = null, // the header already offers to open another folder
            )
            state.path == null -> MessageState(
                title = stringResource(Res.string.repository_empty_title),
                // The hint asks to pick a folder, so it only makes sense with a picker.
                detail = if (onOpenRepository != null) stringResource(Res.string.repository_empty_hint) else null,
                onOpenRepository = onOpenRepository,
            )
            else -> Row(Modifier.fillMaxSize()) {
                RefsSidebar(state.refs, state.currentBranch, Modifier.width(240.dp).fillMaxHeight())
                VerticalDivider()
                CommitList(state.commits, Modifier.weight(1f).fillMaxHeight())
                VerticalDivider()
                DetailsPlaceholder(Modifier.width(320.dp).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun RepositoryHeader(path: String, currentBranch: String, onOpenRepository: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(folderName(path), style = MaterialTheme.typography.titleMedium)
        if (currentBranch.isNotEmpty()) {
            Text(
                stringResource(Res.string.repository_on_branch, branchLabel(currentBranch)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.weight(1f))
        if (onOpenRepository != null) {
            OutlinedButton(onClick = onOpenRepository) {
                Text(stringResource(Res.string.repository_open))
            }
        }
    }
}

@Composable
private fun MessageState(title: String, detail: String?, onOpenRepository: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (detail != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (onOpenRepository != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onOpenRepository) {
                Text(stringResource(Res.string.repository_open))
            }
        }
    }
}

@Composable
private fun RefsSidebar(refs: List<Ref>, currentBranch: String, modifier: Modifier) {
    val sections: List<Pair<StringResource, List<Ref>>> = listOf(
        Res.string.refs_local_branches to refs.filter { it.type == RefType.LOCAL_BRANCH },
        Res.string.refs_remote_branches to refs.filter { it.type == RefType.REMOTE_BRANCH },
        Res.string.refs_tags to refs.filter { it.type == RefType.TAG },
    ).filter { (_, refsInSection) -> refsInSection.isNotEmpty() }

    LazyColumn(modifier.padding(vertical = 8.dp)) {
        if (sections.isEmpty()) {
            item { PanelHint(stringResource(Res.string.refs_empty)) }
        }
        sections.forEach { (title, refsInSection) ->
            item { SectionTitle(stringResource(title)) }
            items(refsInSection, key = { it.fullName }) { ref ->
                val isCurrent = ref.type == RefType.LOCAL_BRANCH && ref.name == currentBranch
                Text(
                    ref.name,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CommitList(commits: List<Commit>, modifier: Modifier) {
    Column(modifier) {
        SectionTitle(stringResource(Res.string.commits_title))
        if (commits.isEmpty()) {
            PanelHint(stringResource(Res.string.commits_empty))
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(commits, key = { it.id }) { commit -> CommitRow(commit) }
            }
        }
    }
}

@Composable
private fun CommitRow(commit: Commit) {
    val time = remember(commit.timestampEpochSeconds) { formatCommitTime(commit.timestampEpochSeconds) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            commit.shortMessage,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val meta = MaterialTheme.typography.bodySmall
            val metaColor = MaterialTheme.colorScheme.onSurfaceVariant
            Text(commit.authorName, style = meta, color = metaColor, maxLines = 1)
            Text(time, style = meta, color = metaColor, maxLines = 1)
            Text(commit.id.take(7), style = meta, color = metaColor, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun DetailsPlaceholder(modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        PanelHint(stringResource(Res.string.details_placeholder))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PanelHint(text: String) {
    Text(
        text,
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

// On a detached HEAD, currentBranch is a full commit id: shorten it like the commit list does.
private fun branchLabel(currentBranch: String): String =
    if (currentBranch.length == 40 && currentBranch.all { it in "0123456789abcdef" }) {
        currentBranch.take(7)
    } else {
        currentBranch
    }

private fun folderName(path: String): String =
    path.trimEnd('/', '\\').substringAfterLast('/').substringAfterLast('\\')
