package com.gdgnantes.devfest.androidapp.ui.screens.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.androidapp.ui.screens.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
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
        Text(
            modifier = modifier.padding(it),
            text = "Settings"
        )
    }
}