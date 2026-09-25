package com.gdgnantes.devfest.androidapp.ui.screens.settings

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
internal fun SettingsTileSubtitle(subtitle: @Composable () -> Unit) {
    ProvideTextStyle(value = MaterialTheme.typography.titleSmall) {
        CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = 0.6f)) {
            subtitle()
        }
    }
}
