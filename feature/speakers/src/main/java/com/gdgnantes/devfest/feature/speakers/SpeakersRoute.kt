package com.gdgnantes.devfest.feature.speakers

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SocialItem
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.feature.speakers.details.SpeakerLayout
import com.gdgnantes.devfest.feature.speakers.list.Speakers
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    speakerId: String,
    viewModel: SpeakerViewModel = koinViewModel { parametersOf(speakerId) },
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
