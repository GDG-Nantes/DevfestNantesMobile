package com.gdgnantes.devfest.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.feature.settings.datacollection.DataCollectionSettingsScreen
import com.gdgnantes.devfest.feature.settings.legal.LegalScreen

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    versionName: String,
    versionCode: Int,
    onBackClick: () -> Unit,
    onLegalClick: () -> Unit,
    onOpenDataSharing: () -> Unit,
    onSupportClick: () -> Unit,
) {
    Settings(
        modifier = modifier,
        versionName = versionName,
        versionCode = versionCode,
        onBackClick = onBackClick,
        onLegalClick = onLegalClick,
        onOpenDataSharing = onOpenDataSharing,
        onSupportClick = onSupportClick
    )
}

@Composable
fun DataCollectionSettingsRoute(
    onBackClick: () -> Unit,
) {
    DataCollectionSettingsScreen(
        onBackClick = onBackClick
    )
}

@Composable
fun LegalRoute(
    onBackClick: () -> Unit,
) {
    LegalScreen(
        onBackClick = onBackClick
    )
}
