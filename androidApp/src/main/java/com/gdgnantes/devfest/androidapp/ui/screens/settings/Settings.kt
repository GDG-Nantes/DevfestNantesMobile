package com.gdgnantes.devfest.androidapp.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.androidapp.ui.screens.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLegalClick: () -> Unit,
    onOpenDataSharing: () -> Unit,
    onSupportClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(id = Screen.Settings.title),
                modifier = Modifier.testTag("topAppBar"),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(id = R.string.action_back)
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = modifier
                .padding(it)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.content_description_data_collection_logo)
                    )
                },
                title = stringResource(id = R.string.screen_data_collection),
                subtitle = stringResource(
                    id = R.string.settings_data_collection_subtitle
                ),
                onClick = onOpenDataSharing
            )

            SettingsItem(
                title = stringResource(id = R.string.settings_version_support),
                subtitle = stringResource(id = R.string.settings_version_support_subtitle),
                onClick = onSupportClick
            )

            SettingsItem(
                title = stringResource(id = R.string.settings_legal),
                onClick = onLegalClick
            )

            SettingsItem(
                title = stringResource(id = R.string.settings_version_label),
                subtitle = stringResource(
                    R.string.app_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                )
            )
        }
    }
}