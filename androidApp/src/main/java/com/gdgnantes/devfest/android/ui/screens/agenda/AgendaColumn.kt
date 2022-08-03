package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdgnantes.devfest.model.Session

@Composable
fun AgendaColumn(
    sessionsPerStartTime: List<Session>,
    onSessionClick: (Session) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxHeight()
    ) {
        items(sessionsPerStartTime) { session ->
            AgendaRow(
                session = session,
                onSessionClick = onSessionClick
            )
        }
    }
}