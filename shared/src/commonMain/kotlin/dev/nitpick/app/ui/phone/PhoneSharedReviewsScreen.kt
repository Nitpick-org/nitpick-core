package dev.nitpick.app.ui.phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nitpick.app.domain.SharedReview
import dev.nitpick.app.ui.repository.RepositoryUiState
import dev.nitpick.app.ui.repository.SharedReviewList
import dev.nitpick.app.ui.theme.NitpickTheme
import nitpick.shared.generated.resources.Res
import nitpick.shared.generated.resources.shared_title
import org.jetbrains.compose.resources.stringResource

/** Reviews shared for this repository from another device; one tap opens its nits on the Review screen. */
@Composable
fun PhoneSharedReviewsScreen(
    state: RepositoryUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpen: (SharedReview) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(NitpickTheme.colors.canvas)) {
        PhoneTopBar(title = stringResource(Res.string.shared_title), onBack = onBack)
        SharedReviewList(
            state = state,
            onRefresh = onRefresh,
            onOpen = onOpen,
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            // 56 dp rows, like the nit rows of the phone Review screen.
            rowMinHeight = 56.dp,
        )
    }
}
