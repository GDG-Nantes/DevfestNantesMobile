package com.gdgnantes.devfest.androidapp.ui.screens.speakers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.Speaker

@Composable
fun SpeakerLayout(
    modifier: Modifier = Modifier,
    viewModel: SpeakerViewModel,
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    SpeakerLayout(
        modifier = modifier,
        speaker = viewModel.speaker.collectAsState(),
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick
    )
}

@Composable
fun SpeakerLayout(
    modifier: Modifier = Modifier,
    speaker: State<Speaker?>,
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    SpeakerLayout(
        modifier = modifier,
        speaker = speaker.value,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick
    )
}

@Composable
fun SpeakerLayout(
    modifier: Modifier = Modifier,
    speaker: Speaker?,
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = speaker?.name ?: stringResource(id = R.string.screen_speaker),
                modifier = Modifier.testTag("topAppBar"),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(id = R.string.action_back)
                        )
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier.padding(it)
        ) {
            speaker?.let {
                SpeakerDetails(
                    modifier = Modifier.fillMaxSize(),
                    speaker = speaker,
                    onSocialLinkClick = onSocialLinkClick
                )
            }
        }
    }
}
