package com.gdgnantes.devfest.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.core.model.Partner
import com.gdgnantes.devfest.core.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.feature.about.components.GithubCard
import com.gdgnantes.devfest.feature.about.partners.Partners

@Composable
fun About(
    modifier: Modifier = Modifier,
    versionName: String,
    versionCode: Int,
    onCodeOfConductClick: () -> Unit,
    onDevFestNantesWebsiteClick: () -> Unit,
    onFacebookClick: () -> Unit,
    onTwitterClick: () -> Unit,
    onLinkedInClick: () -> Unit,
    onYouTubeClick: () -> Unit,
    onPartnerClick: (Partner) -> Unit,
    onLocalCommunitiesClick: () -> Unit,
    onGithubClick: () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AboutHeader(modifier.padding(horizontal = 8.dp))

        AboutLinks(
            modifier.padding(horizontal = 8.dp),
            onCodeOfConductClick = onCodeOfConductClick,
            onDevFestNantesWebsiteClick = onDevFestNantesWebsiteClick
        )

        AboutSocial(
            modifier.padding(horizontal = 8.dp),
            onFacebookClick = onFacebookClick,
            onTwitterClick = onTwitterClick,
            onLinkedInClick = onLinkedInClick,
            onYouTubeClick = onYouTubeClick
        )

        Partners(
            modifier.padding(horizontal = 8.dp),
            onPartnerClick = onPartnerClick
        )

        AboutLocalCommunities(
            modifier.padding(horizontal = 8.dp),
            onClick = onLocalCommunitiesClick
        )

        GithubCard(
            modifier
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterHorizontally),
            onCLick = onGithubClick
        )

        AboutVersion(modifier.padding(8.dp), versionName = versionName, versionCode = versionCode)
    }
}

@Preview
@Composable
fun AboutPreview() {
    DevFestNantesTheme {
        About(
            versionName = "2025.10.00",
            versionCode = 37,
            onCodeOfConductClick = {},
            onDevFestNantesWebsiteClick = {},
            onFacebookClick = {},
            onTwitterClick = {},
            onLinkedInClick = {},
            onYouTubeClick = {},
            onPartnerClick = {},
            onLocalCommunitiesClick = {},
            onGithubClick = {}
        )
    }
}
