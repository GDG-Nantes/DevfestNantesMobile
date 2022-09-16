package com.gdgnantes.devfest.androidapp.ui.screens.session

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.components.SocialIcon
import com.gdgnantes.devfest.androidapp.ui.theme.DevFest_NantesTheme
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
                contentDescription = stringResource(
                    id = R.string.content_description_speaker_picture,
                    speaker.name
                )
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

                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (socialsItem in speaker.socials.orEmpty()
                        .filter { it.link != null && it.type != null }) {
                        if (socialsItem.type == SocialsType.TWITTER) {
                            SocialIcon(
                                resourceId = R.drawable.ic_network_twitter,
                                contentDescription = stringResource(
                                    id = R.string.content_description_logo,
                                    "Twitter"
                                ),
                                onClick = { socialsItem.link?.let { onSocialLinkClick(it) } }
                            )
                        } else {
                            SocialIcon(
                                resourceId = R.drawable.ic_network_web,
                                contentDescription = stringResource(
                                    id = R.string.content_description_speaker_website_icon
                                ),
                                onClick = { socialsItem.link?.let { onSocialLinkClick(it) } }
                            )
                        }
                    }
                }
            }
        }


    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(
    "Night mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SpeakerDetailsPreview() {
    DevFest_NantesTheme {
        SpeakerDetails(speaker = buildSpeakerStub(), onSocialLinkClick = {})
    }
}