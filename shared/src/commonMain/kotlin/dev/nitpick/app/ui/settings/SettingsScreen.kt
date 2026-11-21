package dev.nitpick.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nitpick.app.settings.LlmProvider
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.ThemeMode
import dev.nitpick.app.settings.knownModels
import dev.nitpick.app.ui.repository.label
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.action_back
import nitpick.shared.generated.resources.settings_api_key
import nitpick.shared.generated.resources.settings_model
import nitpick.shared.generated.resources.settings_provider
import nitpick.shared.generated.resources.settings_theme
import nitpick.shared.generated.resources.settings_title
import nitpick.shared.generated.resources.theme_dark
import nitpick.shared.generated.resources.theme_light
import nitpick.shared.generated.resources.theme_system
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private val ScreenPadding = 24.dp
private val SectionSpacing = 16.dp
private val ChoiceTextSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 14.sp)

internal val ThemeMode.label: StringResource
    get() = when (this) {
        ThemeMode.SYSTEM -> Res.string.theme_system
        ThemeMode.LIGHT -> Res.string.theme_light
        ThemeMode.DARK -> Res.string.theme_dark
    }

@Composable
fun SettingsScreen(settings: Settings, onChange: ((Settings) -> Settings) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).safeContentPadding().padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        TextButton(onClick = onBack) { Text(stringResource(Res.string.action_back)) }
        Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.headlineMedium)

        Text(stringResource(Res.string.settings_theme), style = MaterialTheme.typography.titleMedium)
        ChoiceRow(
            options = ThemeMode.entries,
            selected = settings.themeMode,
            label = { it.label },
            onSelect = { mode -> onChange { it.copy(themeMode = mode) } },
        )

        Text(stringResource(Res.string.settings_provider), style = MaterialTheme.typography.titleMedium)
        ChoiceRow(
            options = LlmProvider.entries,
            selected = settings.provider,
            label = { it.label },
            onSelect = { provider ->
                onChange { it.copy(provider = provider, model = provider.knownModels.first()) }
            },
        )

        ModelField(
            model = settings.model,
            suggestions = settings.provider.knownModels,
            onChange = { model -> onChange { it.copy(model = model) } },
        )

        OutlinedTextField(
            value = settings.apiKey,
            onValueChange = { key -> onChange { it.copy(apiKey = key) } },
            label = { Text(stringResource(Res.string.settings_api_key)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelField(model: String, suggestions: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = model,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.settings_model)) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = { onChange(suggestion); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun <T> ChoiceRow(
    options: List<T>,
    selected: T,
    label: (T) -> StringResource,
    onSelect: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = {},
            ) {
                Text(stringResource(label(option)), maxLines = 1, autoSize = ChoiceTextSize)
            }
        }
    }
}
