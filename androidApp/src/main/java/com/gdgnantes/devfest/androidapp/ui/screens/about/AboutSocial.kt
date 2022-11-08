package com.gdgnantes.devfest.androidapp.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.components.SocialIcon
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme

@Composable
fun AboutSocial(
    modifier: Modifier = Modifier,
    onFacebookClick: () -> Unit,
    onTwitterClick: () -> Unit,
    onLinkedInClick: () -> Unit,
    onYouTubeClick: () -> Unit,
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                R.string.about_social_title
            ),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SocialIcon(
                resourceId = R.drawable.ic_network_facebook,
                contentDescription = stringResource(
                    id = R.string.content_description_logo,
                    "Facebook"
                ),
                onClick = onFacebookClick
            )

            SocialIcon(
                resourceId = R.drawable.ic_network_twitter,
                contentDescription = stringResource(
                    id = R.string.content_description_logo,
                    "Twitter"
                ),
                onClick = onTwitterClick
            )

            SocialIcon(
                resourceId = R.drawable.ic_network_linkedin,
                contentDescription = stringResource(
                    id = R.string.content_description_logo,
                    "LinkedIn"
                ),
                onClick = onLinkedInClick
            )

            SocialIcon(
                resourceId = R.drawable.ic_network_youtube,
                contentDescription = stringResource(
                    id = R.string.content_description_logo,
                    "Youtube"
                ),
                onClick = onYouTubeClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AboutSocialPreview() {
    DevFestNantesTheme {
        Scaffold {
            AboutSocial(modifier = Modifier.padding(it),
                onFacebookClick = {},
                onTwitterClick = {},
                onLinkedInClick = {},
                onYouTubeClick = {})
        }
    }
}
