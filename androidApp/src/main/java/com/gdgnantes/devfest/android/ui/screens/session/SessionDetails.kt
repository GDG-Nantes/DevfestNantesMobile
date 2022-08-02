package com.gdgnantes.devfest.android.ui.screens.session

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.model.Session

@Composable
fun SessionDetails(
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier
) {
    Session(sessionState = viewModel.session.collectAsState())
}

@Composable
fun Session(
    sessionState: State<Session?>
) {
    sessionState.value?.let { session ->
        Session(session = session)
    }
}

@Composable
fun Session(
    session: Session
) {
    Text(session.title)
}