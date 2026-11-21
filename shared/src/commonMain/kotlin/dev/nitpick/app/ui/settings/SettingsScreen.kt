package dev.nitpick.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.ThemeMode
import dev.nitpick.app.settings.knownModels
import dev.nitpick.app.ui.components.NitpickCard
import dev.nitpick.app.ui.components.NitpickRadioGroup
import dev.nitpick.app.ui.components.NitpickSegmentedControl
import dev.nitpick.app.ui.phone.PhoneTopBar
import dev.nitpick.app.ui.repository.label
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.provider_cloud_hint
import nitpick.shared.generated.resources.provider_ollama_hint
import nitpick.shared.generated.resources.settings_model
import nitpick.shared.generated.resources.settings_provider
import nitpick.shared.generated.resources.settings_theme
import nitpick.shared.generated.resources.settings_title
import nitpick.shared.generated.resources.theme_dark
import nitpick.shared.generated.resources.theme_light
import nitpick.shared.generated.resources.theme_system
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Touch targets of the UI handoff §4.4: 48 dp controls, 52 dp radio rows.
private val TouchControlHeight = 48.dp
private val RadioRowHeight = 52.dp

internal val ThemeMode.label: StringResource
    get() = when (this) {
        ThemeMode.SYSTEM -> Res.string.theme_system
        ThemeMode.LIGHT -> Res.string.theme_light
        ThemeMode.DARK -> Res.string.theme_dark
    }

private val LlmProvider.hint: StringResource
    get() = if (this == LlmProvider.OLLAMA) Res.string.provider_ollama_hint else Res.string.provider_cloud_hint

/** Phone settings (UI handoff §4.4): every change is saved at once, there is no Save button on a phone. */
@Composable
fun SettingsScreen(settings: Settings, onChange: ((Settings) -> Settings) -> Unit, onBack: () -> Unit) {
    val themeLabels = ThemeMode.entries.associateWith { stringResource(it.label) }
    val providerLabels = LlmProvider.entries.associateWith { stringResource(it.label) }
    val providerHints = LlmProvider.entries.associateWith { stringResource(it.hint) }
    // Background first so the canvas colour also fills the status-bar inset.
    Column(Modifier.fillMaxSize().background(NitpickTheme.colors.canvas).safeContentPadding()) {
        PhoneTopBar(title = stringResource(Res.string.settings_title), onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NitpickCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(Res.string.settings_theme)
                NitpickSegmentedControl(
                    options = ThemeMode.entries,
                    selected = settings.themeMode,
                    label = { themeLabels.getValue(it) },
                    onSelect = { mode -> onChange { it.copy(themeMode = mode) } },
                    modifier = Modifier.fillMaxWidth(),
                    height = TouchControlHeight,
                )
            }
            NitpickCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(Res.string.settings_provider)
                NitpickRadioGroup(
                    options = LlmProvider.entries,
                    selected = settings.provider,
                    label = { providerLabels.getValue(it) },
                    onSelect = { provider -> onChange { it.copy(provider = provider, model = provider.knownModels.first()) } },
                    subtitle = { providerHints.getValue(it) },
                    vertical = true,
                    rowHeight = RadioRowHeight,
                )
                SectionTitle(Res.string.settings_model)
                ModelSelector(
                    model = settings.model,
                    models = settings.provider.knownModels,
                    onSelect = { model -> onChange { it.copy(model = model) } },
                    modifier = Modifier.fillMaxWidth(),
                    height = TouchControlHeight,
                )
                if (settings.provider != LlmProvider.OLLAMA) {
                    ApiKeyField(
                        apiKey = settings.apiKey,
                        provider = settings.provider,
                        onChange = { key -> onChange { it.copy(apiKey = key) } },
                        modifier = Modifier.fillMaxWidth(),
                        minHeight = TouchControlHeight,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(resource: StringResource) {
    Text(stringResource(resource), style = NitpickTheme.typography.label, color = NitpickTheme.colors.textSecondary)
}
