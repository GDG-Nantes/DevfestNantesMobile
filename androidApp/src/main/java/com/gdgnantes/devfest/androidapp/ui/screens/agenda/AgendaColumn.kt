package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import android.content.res.Resources
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.gdgnantes.devfest.androidapp.utils.getDateFromIso8601
import com.gdgnantes.devfest.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    LaunchedEffect(key1 = sessionsPerStartTime) {
        val index = getCurrentStartTimeIndex(sessionsGroupedByStartTime)
        listState.scrollToItem(index)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxHeight()
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
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Row(
            modifier = modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint =  MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = prettyTime,
                style = MaterialTheme.typography.headlineMedium,
            )
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

private suspend fun getCurrentStartTimeIndex(sessionsGroupedByStartTime: Map<String, List<Session>>): Int {
    var index = 0
    return withContext(Dispatchers.IO) {
        val now = Date()
        sessionsGroupedByStartTime.forEach { entry ->
            entry.value.firstOrNull()?.scheduleSlot?.endDate?.let {
                getDateFromIso8601(it)?.let { sessionEndDate ->
                    if (now > sessionEndDate) {
                        index += entry.value.size + 1 //Adds count of sessions per time + 1 for the header.
                    } else {
                        return@withContext index
                    }
                }
            }
        }
        index
    }
}