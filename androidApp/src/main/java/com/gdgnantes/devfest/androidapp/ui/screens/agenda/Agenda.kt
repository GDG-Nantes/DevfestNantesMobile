@file:OptIn(ExperimentalMaterial3Api::class)

package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.utils.SessionFilter
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.Room
import com.gdgnantes.devfest.model.Session

@Composable
fun Agenda(
    modifier: Modifier = Modifier,
    agendaFilterDrawerState: DrawerState,
    viewModel: AgendaViewModel = hiltViewModel(),
    onSessionClick: ((Session) -> Unit)
) {
    AgendaLayout(
        modifier = modifier,
        agendaFilterDrawerState = agendaFilterDrawerState,
        uiState = viewModel.uiState.collectAsState(),
        days = viewModel.days.collectAsState(),
        rooms = viewModel.rooms.collectAsState(),
        sessionFilters = viewModel.sessionFilters.collectAsState(),
        onRefresh = viewModel::onRefresh,
        onSessionClick = onSessionClick,
        onSessionFiltersChanged = viewModel::onSessionFiltersChanged
    )
}

@Composable
fun AgendaLayout(
    modifier: Modifier = Modifier,
    agendaFilterDrawerState: DrawerState,
    uiState: State<UiState>,
    days: State<Map<Int, AgendaDay>>,
    rooms: State<Set<Room>>,
    sessionFilters: State<Set<SessionFilter>>,
    onRefresh: () -> Unit,
    onSessionClick: ((Session) -> Unit),
    onSessionFiltersChanged: (Set<SessionFilter>) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = agendaFilterDrawerState,
        drawerContent = {
            ModalDrawerSheet {
                AgendaFilterDrawer(
                    rooms = rooms.value,
                    sessionFilters = sessionFilters.value,
                    onSessionFiltersChanged = onSessionFiltersChanged,
                )
            }
        },
        content = {
            AgendaPager(
                modifier = modifier,
                initialPageIndex = 0,
                days = days.value,
                uiState = uiState.value,
                onRefresh = onRefresh,
                onSessionClick = onSessionClick
            )
        }
    )
}