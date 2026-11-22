package dev.nitpick.app.ui

import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.ic_app_dark
import nitpick.shared.generated.resources.ic_app_light
import nitpick.shared.generated.resources.logo_socraft_dark
import nitpick.shared.generated.resources.logo_socraft_light
import kotlin.test.Test
import kotlin.test.assertEquals

class NitpickAppIconTest {
    @Test
    fun picksTheVariantOfTheTheme() {
        assertEquals(Res.drawable.ic_app_light, appIconResource(isDark = false))
        assertEquals(Res.drawable.ic_app_dark, appIconResource(isDark = true))
    }

    @Test
    fun socraftLogoFollowsTheTheme() {
        assertEquals(Res.drawable.logo_socraft_light, socraftLogoResource(isDark = false))
        assertEquals(Res.drawable.logo_socraft_dark, socraftLogoResource(isDark = true))
    }
}
