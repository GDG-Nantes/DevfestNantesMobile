package com.gdgnantes.devfest.feature.sessiondetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.core.model.SocialItem
import com.gdgnantes.devfest.core.model.Speaker

@Composable
fun SessionDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel,
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
