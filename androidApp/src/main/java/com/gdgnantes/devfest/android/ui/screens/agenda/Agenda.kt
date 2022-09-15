package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.Session

@Composable
fun Agenda(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = hiltViewModel(),
    onSessionClick: ((Session) -> Unit)
) {
    AgendaLayout(
        modifier = modifier,
        uiState = viewModel.uiState.collectAsState(),
        days = viewModel.days.collectAsState(),
        onRefresh = viewModel::onRefresh,
        onSessionClick = onSessionClick
    )
}

@Composable
fun AgendaLayout(
    modifier: Modifier = Modifier,
    uiState: State<UiState>,
    days: State<Map<Int, AgendaDay>>,
    onRefresh: () -> Unit,
    onSessionClick: ((Session) -> Unit)
) {
    AgendaPager(
        modifier = modifier,
        initialPageIndex = 0,
        days = days.value,
        uiState = uiState.value,
        onRefresh = onRefresh,
        onSessionClick = onSessionClick
    )
}