package dev.nitpick.app.ui.settings

import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import dev.nitpick.app.settings.ThemeMode
import dev.nitpick.app.settings.withRecentRepository
import kotlin.test.assertNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val stored = Settings(apiKey = "sk-stored", themeMode = ThemeMode.DARK)
    private val store = SlowStore(stored)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun anUpdateFiredBeforeTheFileIsReadKeepsTheStoredSettings() {
        val viewModel = SettingsViewModel(store)

        viewModel.update { it.copy(recentRepositories = listOf("/work/demo")) }
        assertEquals(false, viewModel.isLoaded.value)
        store.finishLoading()
        assertEquals(true, viewModel.isLoaded.value)

        val expected = stored.copy(recentRepositories = listOf("/work/demo"))
        assertEquals(expected, viewModel.settings.value)
        assertEquals(listOf(expected), store.saved)
    }

    @Test
    fun reopeningTheRepositoryAlreadyOnTopDoesNotWriteTheFile() {
        val viewModel = SettingsViewModel(store)
        store.finishLoading()

        viewModel.update { it.withRecentRepository("/work/demo") }
        viewModel.update { it.withRecentRepository("/work/demo") }

        assertEquals(1, store.saved.size)
    }

    @Test
    fun editingThenDiscardingTheDraftSavesNothing() {
        val viewModel = SettingsViewModel(store)
        store.finishLoading()

        viewModel.startEditing()
        viewModel.editDraft { it.copy(themeMode = ThemeMode.LIGHT) }
        viewModel.discardDraft()

        assertNull(viewModel.draft.value)
        assertEquals(stored, viewModel.settings.value)
        assertEquals(emptyList(), store.saved)
    }

    @Test
    fun savingTheDraftWritesOnceAndKeepsWhatChangedUnderneath() {
        val viewModel = SettingsViewModel(store)
        store.finishLoading()

        viewModel.startEditing()
        viewModel.editDraft { it.copy(themeMode = ThemeMode.LIGHT, apiKey = "sk-new") }
        viewModel.update { it.withRecentRepository("/work/demo") }
        viewModel.saveDraft()

        val expected = stored.copy(themeMode = ThemeMode.LIGHT, apiKey = "sk-new", recentRepositories = listOf("/work/demo"))
        assertEquals(expected, viewModel.settings.value)
        assertEquals(expected, store.saved.last())
        assertEquals(2, store.saved.size)
        assertNull(viewModel.draft.value)
    }

    @Test
    fun aDraftStartedBeforeTheFileIsReadWaitsForTheStoredSettings() {
        val viewModel = SettingsViewModel(store)

        viewModel.startEditing()
        assertNull(viewModel.draft.value)

        store.finishLoading()
        assertEquals(stored, viewModel.draft.value)
    }

    /** A store whose load only completes when the test says so. */
    private class SlowStore(private val stored: Settings) : SettingsStore {
        private val loading = CompletableDeferred<Settings>()
        val saved = mutableListOf<Settings>()

        fun finishLoading() = loading.complete(stored)

        override suspend fun load(): Settings = loading.await()

        override suspend fun save(settings: Settings) {
            saved += settings
        }
    }
}
