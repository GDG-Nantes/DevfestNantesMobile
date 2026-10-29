package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Session
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

/**
 * Shared day and time-slot grouping of the agenda (D-02, D-06, D-07).
 *
 * Sessions are assigned to days by the [DayAssignment], grouped into slots keyed by their start
 * [Instant] and ordered chronologically. Inside a slot, sessions follow the room order
 * (`room.sortIndex`, then `room.name`, a session without a room first, ties in input order): this
 * is the v1.0 `RoomSortIndex` order. v1.0 Android left the in-slot order unspecified (`HashSet`) and
 * v1.0 iOS sorted by room name; both are covered by this one deterministic order.
 *
 * The time zone is an explicit parameter on every platform (D-05, D-19): each caller passes
 * `TimeZone.currentSystemDefault()`. Only [DayAssignment.LocalDateDays] reads it.
 */
class GroupSessionsUseCase {
    fun group(
        sessions: List<Session>,
        dayAssignment: DayAssignment,
        timeZone: TimeZone
    ): List<DaySchedule> {
        val byDay = mutableMapOf<Int, MutableList<Pair<Instant, Session>>>()
        for (session in sessions) {
            val start = Instant.fromEpochMilliseconds(session.scheduleSlot.startDateAsEpochMilliseconds)
            val position = dayAssignment.dayPosition(start, timeZone) ?: continue
            byDay.getOrPut(position) { mutableListOf() }.add(start to session)
        }
        return dayAssignment.dayLabels().mapIndexed { position, label ->
            val slots =
                byDay[position].orEmpty()
                    .sortedBy { (start, _) -> start }
                    .groupBy({ (start, _) -> start }, { (_, session) -> session })
                    .map { (start, slotSessions) -> TimeSlot(start, slotSessions.sortedWith(ROOM_ORDER)) }
            DaySchedule(dayIndex = position + 1, date = label, slots = slots)
        }
    }

    private companion object {
        val ROOM_ORDER: Comparator<Session> = compareBy<Session>({ it.room?.sortIndex }, { it.room?.name })
    }
}
