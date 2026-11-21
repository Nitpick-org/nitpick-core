package dev.nitpick.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Nitpick follows the JetBrains IDE (New UI / Islands) design language: dark graphite
 * chrome, 24–28dp density, one action colour, and semantic colour only for diffs,
 * severity and VCS state. The values below are sampled from IntelliJ IDEA's expUI light
 * and dark themes so the app feels like the IDE the challenge comes from; only the primary
 * family is Nitpick's teal instead of JetBrains blue, which moves to lane 5.
 */
@Immutable
data class NitpickColors(
    val canvas: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceSunken: Color,
    val toolbar: Color,
    val onToolbar: Color,
    val statusBar: Color,
    val onStatusBar: Color,
    val panelHeader: Color,
    val onPanelHeader: Color,
    val editor: Color,
    val gutter: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryHover: Color,
    val primarySoft: Color,
    val selection: Color,
    val onSelection: Color,
    val hover: Color,
    val success: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val danger: Color,
    val dangerSoft: Color,
    val info: Color,
    val infoSoft: Color,
    val suggestion: Color,
    val suggestionSoft: Color,
    val ai: Color,
    val aiSoft: Color,
    val diffAddedBg: Color,
    val diffAddedFg: Color,
    val diffAddedGutter: Color,
    val diffRemovedBg: Color,
    val diffRemovedFg: Color,
    val diffRemovedGutter: Color,
    val diffContextBg: Color,
    val lineNumber: Color,
    val focusRing: Color,
    val lanePalette: List<Color>,
)

@Immutable
data class ToneColors(
    val background: Color,
    val foreground: Color,
    val border: Color,
)

enum class NitpickTone {
    Neutral,
    Accent,
    Success,
    Warning,
    Danger,
    Info,
    Suggestion,
    Ai,
}

fun NitpickColors.tone(tone: NitpickTone): ToneColors = when (tone) {
    NitpickTone.Neutral -> ToneColors(surfaceSunken, textSecondary, border)
    NitpickTone.Accent -> ToneColors(primarySoft, primary, primary.copy(alpha = 0.32f))
    NitpickTone.Success -> ToneColors(successSoft, success, success.copy(alpha = 0.32f))
    NitpickTone.Warning -> ToneColors(warningSoft, warning, warning.copy(alpha = 0.32f))
    NitpickTone.Danger -> ToneColors(dangerSoft, danger, danger.copy(alpha = 0.32f))
    NitpickTone.Info -> ToneColors(infoSoft, info, info.copy(alpha = 0.32f))
    NitpickTone.Suggestion -> ToneColors(suggestionSoft, suggestion, suggestion.copy(alpha = 0.32f))
    NitpickTone.Ai -> ToneColors(aiSoft, ai, ai.copy(alpha = 0.32f))
}

@Immutable
data class NitpickSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 32.dp,
)

@Immutable
data class NitpickRadii(
    val xs: Dp = 4.dp,
    val sm: Dp = 6.dp,
    val md: Dp = 8.dp,
    val lg: Dp = 8.dp,
    val xl: Dp = 10.dp,
    val pill: Dp = 999.dp,
)

@Immutable
data class NitpickSizes(
    val control: Dp = 28.dp,
    val controlLarge: Dp = 32.dp,
    val row: Dp = 24.dp,
    val header: Dp = 40.dp,
    val toolWindowHeader: Dp = 28.dp,
    val statusBar: Dp = 22.dp,
    val sidebar: Dp = 240.dp,
    val inspector: Dp = 320.dp,
    val icon: Dp = 16.dp,
    val iconLarge: Dp = 20.dp,
)

val NitpickLightColors = NitpickColors(
    canvas = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFFFFFFF),
    surfaceSunken = Color(0xFFF7F8FA),
    toolbar = Color(0xFF27282E),
    onToolbar = Color(0xFFEBECF0),
    statusBar = Color(0xFFF7F8FA),
    onStatusBar = Color(0xFF6C707E),
    panelHeader = Color(0xFFF7F8FA),
    onPanelHeader = Color(0xFF6C707E),
    editor = Color(0xFFFFFFFF),
    gutter = Color(0xFFF0F0F0),
    border = Color(0xFFEBECF0),
    borderStrong = Color(0xFFC9CCD6),
    text = Color(0xFF000000),
    textSecondary = Color(0xFF6C707E),
    textMuted = Color(0xFF818594),
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    primaryHover = Color(0xFF115E59),
    primarySoft = Color(0xFFD2EFEA),
    selection = Color(0xFFD2EFEA),
    onSelection = Color(0xFF000000),
    hover = Color(0xFFEDF7F5),
    success = Color(0xFF208A3C),
    successSoft = Color(0xFFE3F7E7),
    warning = Color(0xFFA46704),
    warningSoft = Color(0xFFFFF5DB),
    danger = Color(0xFFDB3B4B),
    dangerSoft = Color(0xFFFFEBEC),
    info = Color(0xFF3369D6),
    infoSoft = Color(0xFFEDF3FF),
    suggestion = Color(0xFF1F8039),
    suggestionSoft = Color(0xFFE3F7E7),
    ai = Color(0xFF7444D4),
    aiSoft = Color(0xFFF5EDFF),
    diffAddedBg = Color(0xFFBEE6BE),
    diffAddedFg = Color(0xFF000000),
    diffAddedGutter = Color(0xFFAADEAA),
    diffRemovedBg = Color(0xFFD6D6D6),
    diffRemovedFg = Color(0xFF000000),
    diffRemovedGutter = Color(0xFFC8C8C8),
    diffContextBg = Color(0xFFFFFFFF),
    lineNumber = Color(0xFF999999),
    focusRing = Color(0xFF0F766E),
    lanePalette = listOf(
        Color(0xFF0F766E),
        Color(0xFF208A3C),
        Color(0xFFCE6117),
        Color(0xFF7444D4),
        Color(0xFFDB3B4B),
        Color(0xFF3574F0),
        Color(0xFFDF9303),
        Color(0xFF5A5D6B),
    ),
)

val NitpickDarkColors = NitpickColors(
    canvas = Color(0xFF1E1F22),
    surface = Color(0xFF2B2D30),
    surfaceRaised = Color(0xFF393B40),
    surfaceSunken = Color(0xFF1E1F22),
    toolbar = Color(0xFF2B2D30),
    onToolbar = Color(0xFFDFE1E5),
    statusBar = Color(0xFF1E1F22),
    onStatusBar = Color(0xFF9DA0A8),
    panelHeader = Color(0xFF2B2D30),
    onPanelHeader = Color(0xFF9DA0A8),
    editor = Color(0xFF1E1F22),
    gutter = Color(0xFF313335),
    border = Color(0xFF393B40),
    borderStrong = Color(0xFF4E5157),
    text = Color(0xFFDFE1E5),
    textSecondary = Color(0xFF9DA0A8),
    textMuted = Color(0xFF6F737A),
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF0B2E2A),
    primaryHover = Color(0xFF7FF0DE),
    primarySoft = Color(0xFF1F403C),
    selection = Color(0xFF1F403C),
    onSelection = Color(0xFFDFE1E5),
    hover = Color(0xFF393B40),
    success = Color(0xFF5FAD65),
    successSoft = Color(0xFF253627),
    warning = Color(0xFFBA9752),
    warningSoft = Color(0xFF3D3223),
    danger = Color(0xFFDB5C5C),
    dangerSoft = Color(0xFF402929),
    info = Color(0xFF548AF7),
    infoSoft = Color(0xFF25324D),
    suggestion = Color(0xFF5FAD65),
    suggestionSoft = Color(0xFF253627),
    ai = Color(0xFF8150BE),
    aiSoft = Color(0xFF2F2936),
    diffAddedBg = Color(0xFF294436),
    diffAddedFg = Color(0xFFDFE1E5),
    diffAddedGutter = Color(0xFF447152),
    diffRemovedBg = Color(0xFF484A4A),
    diffRemovedFg = Color(0xFFDFE1E5),
    diffRemovedGutter = Color(0xFF656E76),
    diffContextBg = Color(0xFF1E1F22),
    lineNumber = Color(0xFF606366),
    focusRing = Color(0xFF5EEAD4),
    lanePalette = listOf(
        Color(0xFF5EEAD4),
        Color(0xFF5FAD65),
        Color(0xFFE08855),
        Color(0xFF8150BE),
        Color(0xFFDB5C5C),
        Color(0xFF548AF7),
        Color(0xFFD6AE58),
        Color(0xFF9DA0A8),
    ),
)
