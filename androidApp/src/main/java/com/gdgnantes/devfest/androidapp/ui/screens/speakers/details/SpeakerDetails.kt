package com.gdgnantes.devfest.androidapp.ui.screens.speakers.details

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.ui.components.SocialIcon
import com.gdgnantes.devfest.androidapp.ui.screens.speakers.SpeakerPicture
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.stubs.buildSpeakerStub

@Composable
fun SpeakerDetails(
    modifier: Modifier = Modifier,
    speaker: Speaker,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit
) {
    Column(modifier) {
        SpeakerPicture(
            modifier = Modifier
                .size(128.dp)
                .align(Alignment.CenterHorizontally),
            speaker = speaker
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp),
            text = speaker.getFullNameAndCompany(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        speaker.city?.let { city ->
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = city,
                style = MaterialTheme.typography.titleSmall
            )
        }

        Row(
            Modifier
                .padding(top = 8.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (socialsItem in speaker.socials.orEmpty()
                .filter { it.link != null && it.type != null }) {
                socialsItem.link?.let {
                    SocialIcon(
                        modifier = Modifier.size(24.dp),
                        socialItem = socialsItem,
                        onClick = { onSocialLinkClick(socialsItem, speaker) }
                    )
                }
            }
        }

        speaker.bio?.let { bio ->
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
            )
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
    DevFestNantesTheme {
        SpeakerDetails(speaker = buildSpeakerStub(), onSocialLinkClick = { _, _ -> })
    }
}