package com.gdgnantes.devfest.feature.agenda

import com.gdgnantes.devfest.core.domain.agenda.TimeSlot
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * [currentStartTimeIndex] with explicit instants (no wall clock): three groups of 2, 1 and 3
 * sessions ending at 10:00, 11:00 and 12:00 (+02:00).
 */
class CurrentStartTimeIndexTest {
    private fun session(id: String, end: String) =
        Session(
            id = id,
            scheduleSlot = ScheduleSlot(startDate = "2025-10-16T08:00:00+02:00", endDate = end)
        )

    private fun group(prefix: String, size: Int, end: String) =
        List(size) { session("$prefix-$it", end) }

    private val groups: List<TimeSlot> =
        listOf(
            TimeSlot(Instant.parse("2025-10-16T09:00:00+02:00"), group("a", 2, "2025-10-16T10:00:00+02:00")),
            TimeSlot(Instant.parse("2025-10-16T10:00:00+02:00"), group("b", 1, "2025-10-16T11:00:00+02:00")),
            TimeSlot(Instant.parse("2025-10-16T11:00:00+02:00"), group("c", 3, "2025-10-16T12:00:00+02:00"))
        )

    @Test
    fun before_the_first_group_ends_the_index_is_zero() {
        assertEquals(0, currentStartTimeIndex(groups, Instant.parse("2025-10-16T09:30:00+02:00")))
    }

    @Test
    fun exactly_at_the_first_end_the_index_is_still_zero() {
        assertEquals(0, currentStartTimeIndex(groups, Instant.parse("2025-10-16T10:00:00+02:00")))
    }

    @Test
    fun between_the_first_and_second_end_skips_the_first_group_and_its_header() {
        assertEquals(3, currentStartTimeIndex(groups, Instant.parse("2025-10-16T10:30:00+02:00")))
    }

    @Test
    fun after_every_group_ended_the_index_counts_all_rows_and_headers() {
        assertEquals(9, currentStartTimeIndex(groups, Instant.parse("2025-10-16T13:00:00+02:00")))
    }

    @Test
    fun empty_groups_give_index_zero() {
        assertEquals(0, currentStartTimeIndex(emptyList(), Instant.parse("2025-10-16T13:00:00+02:00")))
    }
}
