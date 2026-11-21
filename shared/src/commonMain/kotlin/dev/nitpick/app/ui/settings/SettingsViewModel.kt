package dev.nitpick.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(private val store: SettingsStore) : ViewModel() {

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    /** False until the settings file has been read: screens gated on a setting wait for it. */
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val loaded = viewModelScope.launch {
        _settings.compareAndSet(Settings(), store.load())
        _isLoaded.value = true
    }

    // An update that lands before the file is read would save the defaults over the stored settings.
    fun update(transform: (Settings) -> Settings) {
        viewModelScope.launch {
            loaded.join()
            val current = _settings.value
            val next = transform(current)
            // Reopening the repository already on top must not rewrite the settings file.
            if (next == current) return@launch
            _settings.value = next
            store.save(next)
        }
    }

    private val _draft = MutableStateFlow<Settings?>(null)
    /** The copy the settings dialog edits; nothing is written until [saveDraft]. */
    val draft: StateFlow<Settings?> = _draft.asStateFlow()

    // Opened right after launch, the dialog must copy the stored settings, not the defaults.
    fun startEditing() {
        viewModelScope.launch {
            loaded.join()
            _draft.value = _settings.value
        }
    }

    fun editDraft(transform: (Settings) -> Settings) {
        _draft.update { it?.let(transform) }
    }

    fun discardDraft() {
        _draft.value = null
    }

    // Only the dialog's fields come from the draft: recent repositories or the git account may
    // have changed underneath while it was open.
    fun saveDraft() {
        val draft = _draft.value ?: return
        _draft.value = null
        update {
            it.copy(
                provider = draft.provider,
                model = draft.model,
                apiKey = draft.apiKey,
                themeMode = draft.themeMode,
                gitConnection = draft.gitConnection,
                serverUrl = draft.serverUrl,
            )
        }
    }
}
