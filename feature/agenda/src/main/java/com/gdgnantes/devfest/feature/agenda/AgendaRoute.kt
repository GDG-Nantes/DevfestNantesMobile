package com.gdgnantes.devfest.feature.agenda

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.core.model.Session

@Composable
fun AgendaRoute(
    modifier: Modifier = Modifier,
    agendaFilterDrawerState: DrawerState,
    onSessionClick: (Session) -> Unit
) {
    Agenda(
        modifier = modifier,
        agendaFilterDrawerState = agendaFilterDrawerState,
        onSessionClick = onSessionClick
    )
}
