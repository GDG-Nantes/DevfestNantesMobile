package com.gdgnantes.devfest.feature.sessiondetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.core.model.SocialItem
import com.gdgnantes.devfest.core.model.Speaker
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionDetailRoute(
    modifier: Modifier = Modifier,
    sessionId: String,
    viewModel: SessionViewModel = koinViewModel { parametersOf(sessionId) },
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit,
    onFeedbackFormFallbackLinkClick: (String) -> Unit
) {
    SessionLayout(
        modifier = modifier,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick,
        onFeedbackFormFallbackLinkClick = onFeedbackFormFallbackLinkClick
    )
}
