package com.gdgnantes.devfest.android.ui.screens.session

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdgnantes.devfest.android.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.model.Session

@Composable
fun SessionDetails(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    SessionLayout(
        sessionState = viewModel.session.collectAsState(),
        modifier = modifier,
        onBackClick = onBackClick
    )
}

@Composable
fun SessionLayout(
    sessionState: State<Session?>,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    sessionState.value?.let { session ->
        SessionLayout(
            modifier = modifier,
            session = session,
            onBackClick = onBackClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLayout(
    session: Session,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = session.title,
                modifier = Modifier.testTag("topAppBar"),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }) {
        Text(
            modifier = modifier.padding(it),
            text = session.abstract
        )
    }
}