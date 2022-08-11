package com.gdgnantes.devfest.android.ui.screens.session

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.model.Session
import io.openfeedback.android.OpenFeedback
import io.openfeedback.android.components.SessionFeedbackContainer

@Composable
fun SessionDetails(
    openFeedback: OpenFeedback,
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    SessionLayout(
        openFeedback = openFeedback,
        sessionState = viewModel.session.collectAsState(),
        modifier = modifier,
        onBackClick = onBackClick
    )
}

@Composable
fun SessionLayout(
    openFeedback: OpenFeedback,
    sessionState: State<Session?>,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    sessionState.value?.let { session ->
        SessionLayout(
            openFeedback = openFeedback,
            modifier = modifier,
            session = session,
            onBackClick = onBackClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLayout(
    openFeedback: OpenFeedback,
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
        Column(modifier.padding(it)) {
            Text(
                text = session.abstract
            )

            SessionFeedbackContainer(
                openFeedback = openFeedback,
                sessionId = "173222",
                language = "en",
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

    }
}