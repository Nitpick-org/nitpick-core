package dev.nitpick.app.ui.settings

import dev.nitpick.app.settings.Settings
import dev.nitpick.app.settings.SettingsStore
import dev.nitpick.app.settings.ThemeMode
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
