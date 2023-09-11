package com.gdgnantes.devfest.androidapp.ui.screens.speakers.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.ui.components.LoadingLayout
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.androidapp.ui.screens.speakers.SpeakerViewModel
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.Speaker

@Composable
fun SpeakerLayout(
    modifier: Modifier = Modifier,
    viewModel: SpeakerViewModel,
    onBackClick: () -> Unit,
    onSessionClick: (Session) -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    SpeakerLayout(
        modifier = modifier,
        uiState = viewModel.uiState.collectAsState(),
        sessions = viewModel.sessions.collectAsState(),
        speaker = viewModel.speaker.collectAsState(),
        onBackClick = onBackClick,
        onSessionClick = onSessionClick,
        onSocialLinkClick = onSocialLinkClick
    )
}

@Composable
fun SpeakerLayout(
    modifier: Modifier = Modifier,
    uiState: State<UiState>,
    sessions: State<List<Session>>,
    speaker: State<Speaker?>,
    onBackClick: () -> Unit,
    onSessionClick: (Session) -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    SpeakerLayout(
        modifier = modifier,
        uiState = uiState.value,
        sessions = sessions.value,
        speaker = speaker.value,
        onBackClick = onBackClick,
        onSessionClick = onSessionClick,
        onSocialLinkClick = onSocialLinkClick
    )
}

@Composable
fun SpeakerLayout(
    modifier: Modifier = Modifier,
    uiState: UiState,
    sessions: List<Session>,
    speaker: Speaker?,
    onBackClick: () -> Unit,
    onSessionClick: (Session) -> Unit,
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
        if (uiState == UiState.STARTING) {
            LoadingLayout(
                modifier = modifier
                    .padding(it)
                    .fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                speaker?.let {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        item {
                            SpeakerDetails(
                                modifier = Modifier.fillMaxWidth(),
                                speaker = speaker,
                                onSocialLinkClick = onSocialLinkClick
                            )
                        }

                        item {
                            Text(
                                "Talks",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(sessions) { session ->
                            SpeakerSession(
                                modifier = Modifier.fillMaxWidth(),
                                session = session,
                                onSessionClick = onSessionClick
                            )
                        }
                    }
                }
            }
        }
    }
}
