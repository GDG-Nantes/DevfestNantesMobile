package com.gdgnantes.devfest.feature.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.core.model.Partner

@Composable
fun AboutRoute(
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
    About(
        modifier = modifier,
        versionName = versionName,
        versionCode = versionCode,
        onCodeOfConductClick = onCodeOfConductClick,
        onDevFestNantesWebsiteClick = onDevFestNantesWebsiteClick,
        onFacebookClick = onFacebookClick,
        onTwitterClick = onTwitterClick,
        onLinkedInClick = onLinkedInClick,
        onYouTubeClick = onYouTubeClick,
        onPartnerClick = onPartnerClick,
        onLocalCommunitiesClick = onLocalCommunitiesClick,
        onGithubClick = onGithubClick,
    )
}
