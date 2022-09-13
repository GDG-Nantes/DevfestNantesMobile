package com.gdgnantes.devfest.android.ui.screens.session

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.model.SocialsType
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.stubs.buildSpeakerStub

@Composable
fun SpeakerDetails(
    modifier: Modifier = Modifier,
    speaker: Speaker,
    onSocialLinkClick: (String) -> Unit
) {
    Column(modifier.padding(top = 16.dp)) {
        Row() {
            AsyncImage(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.ic_person_black_24dp),
                error = painterResource(R.drawable.ic_person_black_24dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(speaker.photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = speaker.name
            )

            Column(Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = speaker.getFullNameAndCompany(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                speaker.bio?.let { bio ->
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Row(Modifier.padding(start = 58.dp, top = 4.dp)) {
            val context = LocalContext.current
            for (socialsItem in speaker.socials.orEmpty()
                .filter { it.link != null && it.type != null }) {
                IconButton(
                    onClick = {
                        socialsItem.link?.let { onSocialLinkClick(it) }
                    }
                ) {
                    Icon(
                        painter = painterResource(
                            if (socialsItem.type == SocialsType.TWITTER) {
                                R.drawable.ic_network_twitter
                            } else {
                                R.drawable.ic_network_web
                            }
                        ), contentDescription = "${socialsItem.type.toString()} icon"
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SpeakerDetailsPreview() {
    DevFest_NantesTheme {
        Scaffold {
            SpeakerDetails(speaker = buildSpeakerStub(), onSocialLinkClick = {})
        }
    }
}