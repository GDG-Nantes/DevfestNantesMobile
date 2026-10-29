package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Session
import kotlin.time.Instant

/**
 * One conference day of the agenda: its 1-based [dayIndex], the configured [date] label (see
 * [DayAssignment]) and its time [slots] in chronological order. A configured day without any
 * session is still present, with no slots.
 */
data class DaySchedule(
    val dayIndex: Int,
    val date: String,
    val slots: List<TimeSlot>
)

/**
 * The sessions starting at the same [start] instant, ordered by room (`sortIndex`, then name; a
 * session without a room first; ties keep the store order).
 */
data class TimeSlot(
    val start: Instant,
    val sessions: List<Session>
)
