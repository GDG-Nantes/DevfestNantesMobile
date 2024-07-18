package com.gdgnantes.devfest.androidapp.ui.screens.speakers

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.model.Speaker

@Composable
fun SpeakerPicture(
    modifier: Modifier = Modifier,
    speaker: Speaker
) {
    AsyncImage(
        modifier =
        modifier
            .clip(CircleShape),
        placeholder = painterResource(R.drawable.ic_person_black_24dp),
        error = painterResource(R.drawable.ic_person_black_24dp),
        model =
        ImageRequest.Builder(LocalContext.current)
            .data(speaker.photoUrl)
            .crossfade(true)
            .build(),
        contentDescription =
        stringResource(
            id = R.string.content_description_speaker_picture,
            speaker.name
        )
    )
}
