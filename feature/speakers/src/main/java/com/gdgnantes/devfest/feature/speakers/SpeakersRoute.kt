package com.gdgnantes.devfest.feature.speakers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SocialItem
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.feature.speakers.details.SpeakerLayout
import com.gdgnantes.devfest.feature.speakers.list.Speakers

@Composable
fun SpeakersRoute(
    modifier: Modifier = Modifier,
    onSpeakerClick: (Speaker) -> Unit
) {
    Speakers(
        modifier = modifier,
        onSpeakerClick = onSpeakerClick
    )
}

@Composable
fun SpeakerDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: SpeakerViewModel,
    onBackClick: () -> Unit,
    onSessionClick: (Session) -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    SpeakerLayout(
        modifier = modifier,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onSessionClick = onSessionClick,
        onSocialLinkClick = onSocialLinkClick
    )
}
