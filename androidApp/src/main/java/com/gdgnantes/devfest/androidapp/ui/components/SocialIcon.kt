package com.gdgnantes.devfest.androidapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.SocialType

@Composable
fun SocialIcon(
    modifier: Modifier = Modifier,
    @DrawableRes resourceId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconTint = (if (isSystemInDarkTheme()) Color.White else Color.Black).copy(alpha = 0.5f)
    Icon(
        modifier =
        modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        painter = painterResource(resourceId),
        contentDescription = contentDescription,
        tint = iconTint
    )
}

@Composable
fun SocialIcon(
    modifier: Modifier = Modifier,
    socialItem: SocialItem,
    onClick: () -> Unit
) {
    val type = socialItem.type ?: return
    when (type) {
        SocialType.GITHUB ->
            SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.github,
                contentDescription =
                stringResource(
                id = R.string.content_description_logo,
                "Github"
            ),
            onClick = onClick
        )

        SocialType.LINKEDIN ->
            SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_linkedin,
                contentDescription =
                stringResource(
                id = R.string.content_description_logo,
                "LinkedIn"
            ),
            onClick = onClick
        )

        SocialType.TWITTER ->
            SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_twitter,
                contentDescription =
                stringResource(
                id = R.string.content_description_logo,
                "Twitter"
            ),
            onClick = onClick
        )

        SocialType.FACEBOOK ->
            SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_facebook,
                contentDescription =
                stringResource(
                id = R.string.content_description_logo,
                "Facebook"
            ),
            onClick = onClick
        )

        else ->
            SocialIcon(
            modifier = modifier,
            resourceId = R.drawable.ic_network_web,
                contentDescription = stringResource(R.string.content_description_speaker_website_icon),
            onClick = onClick
        )
    }
}
