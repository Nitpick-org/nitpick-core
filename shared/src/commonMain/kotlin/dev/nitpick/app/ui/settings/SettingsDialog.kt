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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import nitpick.shared.generated.resources.settings_kicker_ai_review
import nitpick.shared.generated.resources.settings_kicker_appearance
import nitpick.shared.generated.resources.settings_model
import nitpick.shared.generated.resources.settings_provider
import nitpick.shared.generated.resources.settings_search
import nitpick.shared.generated.resources.settings_section_general
import nitpick.shared.generated.resources.settings_section_git
import nitpick.shared.generated.resources.settings_section_keymap
import nitpick.shared.generated.resources.settings_section_review
import nitpick.shared.generated.resources.settings_theme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val DialogMaxWidth = 860.dp
private val DialogMaxHeight = 560.dp
private val NavWidth = 220.dp
private val ControlWidth = 300.dp
private val KeyFieldWidth = 380.dp

// Only General has content today; the other sections are listed, muted, to keep the IDE layout.
private val SECTIONS = listOf(
    Res.string.settings_section_general,
    Res.string.settings_section_review,
    Res.string.settings_section_git,
    Res.string.settings_section_keymap,
)

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
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).widthIn(max = DialogMaxWidth).heightIn(max = DialogMaxHeight),
            shape = RoundedCornerShape(NitpickTheme.radii.xl),
            color = NitpickTheme.colors.surface,
            border = BorderStroke(1.dp, NitpickTheme.colors.borderStrong),
        ) {
            Column {
                Row(Modifier.weight(1f)) {
                    SettingsNav(Modifier.width(NavWidth).fillMaxHeight())
                    NitpickVerticalDivider()
                    GeneralSection(
                        draft = draft,
                        onEdit = onEdit,
                        modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(24.dp),
                    )
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
private fun SettingsNav(modifier: Modifier) {
    var query by remember { mutableStateOf("") }
    val labels = SECTIONS.map { stringResource(it) }
    Column(modifier.background(NitpickTheme.colors.canvas).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NitpickTextField(value = query, onValueChange = { query = it }, placeholder = stringResource(Res.string.settings_search))
        labels.filter { it.contains(query, ignoreCase = true) }.forEachIndexed { index, label ->
            val isGeneral = index == 0 && labels[0] == label
            Text(
                text = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isGeneral) NitpickTheme.colors.selection else NitpickTheme.colors.canvas, RoundedCornerShape(NitpickTheme.radii.sm))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = if (isGeneral) NitpickTheme.typography.bodyStrong else NitpickTheme.typography.body,
                color = if (isGeneral) NitpickTheme.colors.onSelection else NitpickTheme.colors.textMuted,
            )
        }
    }
}

@Composable
private fun GeneralSection(draft: Settings, onEdit: ((Settings) -> Settings) -> Unit, modifier: Modifier) {
    // Labels are resolved here: the option lambdas of the controls are not composable.
    val themeLabels = ThemeMode.entries.associateWith { stringResource(it.label) }
    val providerLabels = LlmProvider.entries.associateWith { stringResource(it.label) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Kicker(Res.string.settings_kicker_appearance)
        FieldLabel(Res.string.settings_theme)
        NitpickSegmentedControl(
            options = ThemeMode.entries,
            selected = draft.themeMode,
            label = { themeLabels.getValue(it) },
            onSelect = { mode -> onEdit { it.copy(themeMode = mode) } },
            modifier = Modifier.width(ControlWidth),
        )

        Kicker(Res.string.settings_kicker_ai_review, topPadding = 12.dp)
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
