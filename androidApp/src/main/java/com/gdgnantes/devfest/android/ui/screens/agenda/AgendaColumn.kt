package com.gdgnantes.devfest.android.ui.screens.agenda

import android.content.res.Resources
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.gdgnantes.devfest.android.utils.getDateFromIso8601
import com.gdgnantes.devfest.model.Session
import java.text.DateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaColumn(
    sessionsPerStartTime: List<Session>,
    onSessionClick: (Session) -> Unit,
) {
    val listState = rememberLazyListState()

    val sessionsGroupedByStartTime = groupSessionsByStartTime(sessionsPerStartTime)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxHeight()
    ) {
        sessionsGroupedByStartTime.forEach {
            stickyHeader {
                TimeSeparator(it.key)
            }

            items(it.value) { session ->
                AgendaRow(
                    session = session,
                    onSessionClick = onSessionClick
                )
            }
        }
    }
}

@Composable
fun TimeSeparator(prettyTime: String) {
    Surface(color = MaterialTheme.colors.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                text = prettyTime,
                style = MaterialTheme.typography.caption
            )
            Surface(
                modifier = Modifier
                    .height(0.5.dp)
                    .fillMaxWidth(),
                color = colors.onSurface
            ) {}
        }
    }
}

private fun groupSessionsByStartTime(sessions: List<Session>): Map<String, List<Session>> {
    return sessions.map { session ->
        val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
        val time = if (locale != null) {
            getDateFromIso8601(session.scheduleSlot.startDate)?.run {
                val formatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
                formatter.timeZone = TimeZone.getDefault()
                formatter.format(this)
            } ?: ""
        } else {
            ""
        }
        time to session
    }
        .filter { pair -> pair.first.isNotEmpty() }
        .groupBy(
            keySelector = { it.first }
        ) {
            it.second
        }
}