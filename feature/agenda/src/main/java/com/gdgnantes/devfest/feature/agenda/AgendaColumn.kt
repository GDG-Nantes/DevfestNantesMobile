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
import com.gdgnantes.devfest.core.domain.agenda.TimeSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.utils.toInstantOrNull
import java.text.DateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgendaColumn(
    slots: List<TimeSlot>,
    onSessionClick: (Session) -> Unit,
) {
    val listState = rememberLazyListState()
    // A slot without a label (no system locale) is not rendered, as v1.0 dropped its sessions.
    val labelledSlots =
        slots
            .map { slot -> slot to formatSlotTime(slot.start) }
            .filter { (_, label) -> label.isNotEmpty() }
    val displayedSlots = labelledSlots.map { (slot, _) -> slot }
    val hasAutoScrolled = rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(key1 = slots) {
        if (!hasAutoScrolled.value) {
            listState.scrollToItem(
                currentStartTimeIndex(displayedSlots, Clock.System.now())
            )
            hasAutoScrolled.value = true
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxHeight()
    ) {
        labelledSlots.forEach { (slot, label) ->
            stickyHeader(slot.start.toEpochMilliseconds()) {
                TimeSeparator(prettyTime = label)
            }

            items(
                items = slot.sessions,
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

/**
 * The v1.0 slot header label: the short time of [start] in the device zone and the system locale
 * (empty when the system has no locale, as v1.0 which then dropped every session).
 */
internal fun formatSlotTime(start: Instant): String {
    val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    return if (locale != null) {
        val formatter: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
        formatter.timeZone = TimeZone.getDefault()
        formatter.format(Date(start.toEpochMilliseconds()))
    } else {
        ""
    }
}

/**
 * Index of the first item (header or session row) of the first slot that has not ended at [now].
 * Each ended slot adds its session count plus one for its header. The end of a slot is the end date
 * of its first session by room name (v1.0 took the first of the name-sorted group); slots whose end
 * date cannot be parsed are skipped.
 */
internal fun currentStartTimeIndex(
    slots: List<TimeSlot>,
    now: Instant
): Int {
    var index = 0
    slots.forEach { slot ->
        val end =
            slot.sessions
                .sortedBy { session -> session.room?.name }
                .firstOrNull()
                ?.scheduleSlot
                ?.endDate
                ?.toInstantOrNull()
        if (end != null) {
            if (now > end) {
                index += slot.sessions.size + 1 // Adds count of sessions per slot + 1 for the header.
            } else {
                return index
            }
        }
    }
    return index
}
