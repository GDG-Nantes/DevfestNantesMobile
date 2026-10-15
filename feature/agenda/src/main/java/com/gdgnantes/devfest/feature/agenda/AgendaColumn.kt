package com.gdgnantes.devfest.feature.agenda

import android.content.res.Resources
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.utils.toInstantOrNull
import com.gdgnantes.devfest.core.ui.utils.getDateFromIso8601
import java.text.DateFormat
import java.util.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaColumn(
    sessionsPerStartTime: List<Session>,
    onSessionClick: (Session) -> Unit,
) {
    val listState = rememberLazyListState()
    val sessionsGroupedByStartTime = groupSessionsByStartTime(sessionsPerStartTime)
    val hasAutoScrolled = rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(key1 = sessionsPerStartTime) {
        if (!hasAutoScrolled.value) {
            listState.scrollToItem(
                currentStartTimeIndex(sessionsGroupedByStartTime, Clock.System.now())
            )
            hasAutoScrolled.value = true
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxHeight()
    ) {
        sessionsGroupedByStartTime.forEach { (timeKey, sessions) ->
            stickyHeader(timeKey) {
                TimeSeparator(prettyTime = timeKey)
            }

            items(
                items = sessions.sortedBy { session -> session.room?.sortIndex },
                key = { session -> session.id }
            ) { session ->
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
                tint = MaterialTheme.colorScheme.onBackground
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
        val time =
            if (locale != null) {
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

/**
 * Index of the first item (header or session row) of the first group that has not ended at [now].
 * Each ended group adds its session count plus one for its header. Groups whose end date cannot be
 * parsed are skipped.
 */
internal fun currentStartTimeIndex(
    sessionsGroupedByStartTime: Map<String, List<Session>>,
    now: Instant
): Int {
    var index = 0
    sessionsGroupedByStartTime.forEach { entry ->
        val end = entry.value.firstOrNull()?.scheduleSlot?.endDate?.toInstantOrNull()
        if (end != null) {
            if (now > end) {
                index += entry.value.size + 1 // Adds count of sessions per time + 1 for the header.
            } else {
                return index
            }
        }
    }
    return index
}
