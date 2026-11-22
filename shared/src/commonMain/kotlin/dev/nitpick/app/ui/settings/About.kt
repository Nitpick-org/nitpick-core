package dev.nitpick.app.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.components.NitpickIcon
import dev.nitpick.app.ui.nitpickAppIcon
import dev.nitpick.app.ui.socraftLogo
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.about_repository
import nitpick.shared.generated.resources.about_made_by
import nitpick.shared.generated.resources.about_tagline
import nitpick.shared.generated.resources.about_team
import nitpick.shared.generated.resources.ic_github
import org.jetbrains.compose.resources.stringResource

private data class TeamMember(val name: String, val login: String)

private const val REPOSITORY_URL = "https://github.com/Nitpick-org/nitpick-core"
private const val SOCRAFT_URL = "https://socraft.ch"
private val TEAM = listOf(TeamMember("Thomas", "pepito2t"), TeamMember("Elliot", "zooldyyk"), TeamMember("Gabriel", "dovatgabriel"))
private val AboutIconSize = 48.dp
private val TouchLinkHeight = 44.dp
private val SocraftLogoHeight = 18.dp

/** The app, its source and the three humans behind it; every line opens GitHub in the browser. */
@Composable
internal fun AboutContent(touch: Boolean) {
    val uriHandler = LocalUriHandler.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(painter = nitpickAppIcon(), contentDescription = null, modifier = Modifier.size(AboutIconSize))
        Column {
            Text("Nitpick", style = NitpickTheme.typography.title, color = NitpickTheme.colors.text)
            Text(stringResource(Res.string.about_tagline), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
        }
    }
    GitHubLink(stringResource(Res.string.about_repository), REPOSITORY_URL, touch, uriHandler::openUri)
    Text(stringResource(Res.string.about_team).uppercase(), style = NitpickTheme.typography.kicker, color = NitpickTheme.colors.textMuted)
    TEAM.forEach { member ->
        GitHubLink("${member.name} · @${member.login}", "https://github.com/${member.login}", touch, uriHandler::openUri)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.about_made_by), style = NitpickTheme.typography.small, color = NitpickTheme.colors.textSecondary)
        Image(
            painter = socraftLogo(),
            contentDescription = "socraft",
            modifier = Modifier
                .height(SocraftLogoHeight)
                .clip(RoundedCornerShape(NitpickTheme.radii.sm))
                .clickable { uriHandler.openUri(SOCRAFT_URL) },
        )
    }
}

@Composable
private fun GitHubLink(text: String, url: String, touch: Boolean, open: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NitpickTheme.radii.sm))
            .clickable { open(url) }
            .then(if (touch) Modifier.heightIn(min = TouchLinkHeight) else Modifier)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NitpickIcon(Res.drawable.ic_github, contentDescription = null, tint = NitpickTheme.colors.text)
        Text(text, style = NitpickTheme.typography.body, color = NitpickTheme.colors.primary)
    }
}
