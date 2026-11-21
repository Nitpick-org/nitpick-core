package dev.nitpick.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            val next = transform(_settings.value)
            _settings.value = next
            store.save(next)
        }
    }
}
