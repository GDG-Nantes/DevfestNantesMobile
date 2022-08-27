package com.gdgnantes.devfest.android.ui.screens.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.WebLinks
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme

@Composable
fun AboutSocial(
    modifier: Modifier = Modifier,
    onWeblinkClick: (String) -> Unit
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
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = { onWeblinkClick(WebLinks.SOCIAL_FACEBOOK.url) }),
                painter = painterResource(R.drawable.ic_network_facebook),
                contentDescription = "Twitter"
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = { onWeblinkClick(WebLinks.SOCIAL_TWITTER.url) }),
                painter = painterResource(R.drawable.ic_network_twitter),
                contentDescription = "Twitter"
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = { onWeblinkClick(WebLinks.SOCIAL_LINKEDIN.url) }),
                painter = painterResource(R.drawable.ic_network_linkedin),
                contentDescription = "Twitter"
            )

            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = { onWeblinkClick(WebLinks.SOCIAL_YOUTUBE.url) }),
                painter = painterResource(R.drawable.ic_network_youtube),
                contentDescription = "Twitter"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AboutSocialPreview() {
    DevFest_NantesTheme {
        Scaffold {
            AboutSocial(modifier = Modifier.padding(it), onWeblinkClick = {})
        }
    }
}
