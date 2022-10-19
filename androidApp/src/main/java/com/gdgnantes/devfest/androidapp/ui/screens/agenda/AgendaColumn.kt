package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import android.content.res.Resources
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.gdgnantes.devfest.androidapp.utils.getDateFromIso8601
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
                TimeSeparator(prettyTime = it.key)
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
fun TimeSeparator(
    modifier: Modifier = Modifier,
    prettyTime: String
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                text = prettyTime,
                style = MaterialTheme.typography.labelMedium
            )
            Surface(
                modifier = Modifier
                    .height(0.5.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        .mapValues { (_, value) ->
            value.sortedBy { session -> session.room?.name }
        }
}