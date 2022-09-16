package com.gdgnantes.devfest.androidapp.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SocialIcon(
    modifier: Modifier = Modifier,
    @DrawableRes resourceId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Icon(
        modifier = modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        painter = painterResource(resourceId),
        contentDescription = contentDescription,
        tint = if (isSystemInDarkTheme()) Color.White else LocalContentColor.current.copy(
            alpha = LocalContentAlpha.current
        )
    )
}