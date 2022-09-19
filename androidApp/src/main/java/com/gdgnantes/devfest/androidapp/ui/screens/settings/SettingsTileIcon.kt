package com.gdgnantes.devfest.androidapp.ui.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SettingsTileIcon(
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.size(64.dp),
        contentAlignment = Alignment.Center,
    ) {
        icon?.let {
            CompositionLocalProvider(LocalContentColor provides if (isSystemInDarkTheme()) Color.White else Color.Black) {
                it()
            }
        }
    }
}

@Preview
@Composable
fun SettingsIconPreview() {
    MaterialTheme {
        SettingsTileIcon {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = ""
            )
        }
    }
}
