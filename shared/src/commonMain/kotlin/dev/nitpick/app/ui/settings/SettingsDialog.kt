package dev.nitpick.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import dev.nitpick.app.settings.AppLanguage
import nitpick.shared.generated.resources.language_english
import nitpick.shared.generated.resources.language_french
import nitpick.shared.generated.resources.language_system
import nitpick.shared.generated.resources.settings_language
import nitpick.shared.generated.resources.settings_section_about
import nitpick.shared.generated.resources.settings_section_sharing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.nitpick.app.settings.GitConnection
import dev.nitpick.app.settings.GitHost
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.ThemeMode
import dev.nitpick.app.settings.knownModels
import dev.nitpick.app.ui.components.NitpickButton
import dev.nitpick.app.ui.components.NitpickButtonVariant
import dev.nitpick.app.ui.components.NitpickDivider
import dev.nitpick.app.ui.components.NitpickRadioGroup
import dev.nitpick.app.ui.components.NitpickSegmentedControl
import dev.nitpick.app.ui.components.NitpickTextField
import dev.nitpick.app.ui.components.NitpickVerticalDivider
import dev.nitpick.app.ui.repository.label
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.action_cancel
import nitpick.shared.generated.resources.action_save
import nitpick.shared.generated.resources.connect_github
import nitpick.shared.generated.resources.connect_gitlab
import nitpick.shared.generated.resources.connect_host
import nitpick.shared.generated.resources.settings_kicker_ai_review
import nitpick.shared.generated.resources.settings_kicker_git
import nitpick.shared.generated.resources.settings_kicker_sharing
import nitpick.shared.generated.resources.settings_kicker_appearance
import nitpick.shared.generated.resources.settings_model
import nitpick.shared.generated.resources.settings_provider
import nitpick.shared.generated.resources.settings_search
import nitpick.shared.generated.resources.settings_section_general
import nitpick.shared.generated.resources.settings_section_git
import nitpick.shared.generated.resources.settings_section_review
import nitpick.shared.generated.resources.settings_theme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val DialogMaxWidth = 600.dp
private val DialogMaxHeight = 460.dp
private val NavWidth = 200.dp
private val ContentWidth = 380.dp
private val ControlWidth = 300.dp
private val KeyFieldWidth = 320.dp

private enum class SettingsSection(val label: StringResource) {
    General(Res.string.settings_section_general),
    Review(Res.string.settings_section_review),
    Git(Res.string.settings_section_git),
    Sharing(Res.string.settings_section_sharing),
    About(Res.string.settings_section_about),
}


/**
 * Desktop settings as an IDE dialog (handoff §3.5). It edits a draft and only [onSave] writes
 * it; Esc, click outside and Cancel all go through [onCancel].
 */
@Composable
fun SettingsDialog(
    draft: Settings,
    onEdit: ((Settings) -> Settings) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    var section by rememberSaveable { mutableStateOf(SettingsSection.General) }
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.widthIn(max = DialogMaxWidth).heightIn(max = DialogMaxHeight),
            shape = RoundedCornerShape(NitpickTheme.radii.xl),
            color = NitpickTheme.colors.surface,
            border = BorderStroke(1.dp, NitpickTheme.colors.borderStrong),
        ) {
            Column {
                Row(Modifier.weight(1f)) {
                    SettingsNav(section, onSelect = { section = it }, Modifier.width(NavWidth).fillMaxHeight())
                    NitpickVerticalDivider()
                    // key(section): each section starts at the top with its own scroll state.
                    key(section) {
                    val content = Modifier.width(ContentWidth).fillMaxHeight().verticalScroll(rememberScrollState()).padding(20.dp)
                    when (section) {
                        SettingsSection.General -> GeneralSection(draft, onEdit, content)
                        SettingsSection.Review -> ReviewSection(draft, onEdit, content)
                        SettingsSection.Git -> GitSection(draft, onEdit, content)
                        SettingsSection.Sharing -> SharingSection(draft, onEdit, content)
                        SettingsSection.About -> Column(content, verticalArrangement = Arrangement.spacedBy(14.dp)) { AboutContent(touch = false) }
                    }
                    }
                }
                NitpickDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    NitpickButton(stringResource(Res.string.action_cancel), onCancel, variant = NitpickButtonVariant.Secondary)
                    NitpickButton(stringResource(Res.string.action_save), onSave)
                }
            }
        }
    }
}

@Composable
private fun SettingsNav(selected: SettingsSection, onSelect: (SettingsSection) -> Unit, modifier: Modifier) {
    var query by remember { mutableStateOf("") }
    val labels = SettingsSection.entries.associateWith { stringResource(it.label) }
    Column(modifier.background(NitpickTheme.colors.canvas).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        NitpickTextField(value = query, onValueChange = { query = it }, placeholder = stringResource(Res.string.settings_search))
        SettingsSection.entries.filter { labels.getValue(it).contains(query, ignoreCase = true) }.forEach { section ->
            val isSelected = section == selected
            Text(
                text = labels.getValue(section),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(NitpickTheme.radii.sm))
                    .background(if (isSelected) NitpickTheme.colors.selection else NitpickTheme.colors.canvas)
                    .clickable { onSelect(section) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = if (isSelected) NitpickTheme.typography.bodyStrong else NitpickTheme.typography.body,
                color = if (isSelected) NitpickTheme.colors.onSelection else NitpickTheme.colors.text,
            )
        }
    }
}

@Composable
private fun GeneralSection(draft: Settings, onEdit: ((Settings) -> Settings) -> Unit, modifier: Modifier) {
    // Labels are resolved here: the option lambdas of the controls are not composable.
    val themeLabels = ThemeMode.entries.associateWith { stringResource(it.label) }
    val languageLabels = AppLanguage.entries.associateWith { stringResource(it.label) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker(Res.string.settings_kicker_appearance)
        FieldLabel(Res.string.settings_theme)
        NitpickSegmentedControl(
            options = ThemeMode.entries,
            selected = draft.themeMode,
            label = { themeLabels.getValue(it) },
            onSelect = { mode -> onEdit { it.copy(themeMode = mode) } },
            modifier = Modifier.width(ControlWidth),
        )
        FieldLabel(Res.string.settings_language)
        NitpickSegmentedControl(
            options = AppLanguage.entries,
            selected = draft.language,
            label = { languageLabels.getValue(it) },
            onSelect = { language -> onEdit { it.copy(language = language) } },
            modifier = Modifier.width(ControlWidth),
        )
    }
}

@Composable
private fun ReviewSection(draft: Settings, onEdit: ((Settings) -> Settings) -> Unit, modifier: Modifier) {
    val providerLabels = LlmProvider.entries.associateWith { stringResource(it.label) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker(Res.string.settings_kicker_ai_review)
        FieldLabel(Res.string.settings_provider)
        NitpickRadioGroup(
            options = LlmProvider.entries,
            selected = draft.provider,
            label = { providerLabels.getValue(it) },
            onSelect = { provider -> onEdit { it.copy(provider = provider, model = provider.knownModels.first()) } },
        )
        FieldLabel(Res.string.settings_model)
        ModelSelector(draft.model, draft.provider.knownModels, { model -> onEdit { it.copy(model = model) } }, Modifier.width(ControlWidth))
        ApiKeyField(draft.apiKey, draft.provider, { key -> onEdit { it.copy(apiKey = key) } }, Modifier.width(KeyFieldWidth))
    }
}

@Composable
private fun GitSection(draft: Settings, onEdit: ((Settings) -> Settings) -> Unit, modifier: Modifier) {
    val hostLabels = mapOf(
        GitHost.GITHUB to stringResource(Res.string.connect_github),
        GitHost.GITLAB to stringResource(Res.string.connect_gitlab),
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker(Res.string.settings_kicker_git)
        FieldLabel(Res.string.connect_host)
        NitpickRadioGroup(
            options = GitHost.entries,
            selected = draft.gitConnection?.host ?: GitHost.GITHUB,
            label = { host -> hostLabels.getValue(host) },
            onSelect = { host ->
                onEdit { it.copy(gitConnection = (it.gitConnection ?: GitConnection()).copy(host = host)) }
            },
        )
        GitTokenField(
            token = draft.gitConnection?.token.orEmpty(),
            onChange = { token ->
                onEdit { it.copy(gitConnection = (it.gitConnection ?: GitConnection()).copy(token = token)) }
            },
            modifier = Modifier.width(KeyFieldWidth),
        )
    }
}

@Composable
private fun SharingSection(draft: Settings, onEdit: ((Settings) -> Settings) -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker(Res.string.settings_kicker_sharing)
        ServerUrlField(draft.serverUrl, { url -> onEdit { it.copy(serverUrl = url) } }, Modifier.width(KeyFieldWidth))
        ServerTokenField(draft.serverToken, { token -> onEdit { it.copy(serverToken = token) } }, Modifier.width(KeyFieldWidth))
    }
}

@Composable
private fun Kicker(resource: StringResource, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Text(
        text = stringResource(resource).uppercase(),
        modifier = Modifier.padding(top = topPadding),
        style = NitpickTheme.typography.kicker,
        color = NitpickTheme.colors.textMuted,
    )
}

@Composable
private fun FieldLabel(resource: StringResource) {
    Text(stringResource(resource), style = NitpickTheme.typography.label, color = NitpickTheme.colors.textSecondary)
}
