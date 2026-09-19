package com.gdgnantes.devfest.model

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Value-level regression coverage for [ScheduleSlot]'s epoch-millisecond date parsing and
 * [Agenda.Builder]'s day-bucketing logic.
 *
 * This test exists to close a gap [DevFestNantesStoreContractTest] cannot cover: its
 * `assertNotNull`-style assertions confirm the date logic returns *something*, not that it
 * returns the *correct* something. Every expected value below is an absolute epoch millisecond
 * computed independently from the input ISO string, so a timezone/offset-interpretation
 * regression in kotlinx-datetime moves the numbers here, not just "the app crashes".
 *
 * Written and green on the pre-bump kotlinx-datetime version (0.6.2) so that a red result after
 * the 0.8.0 bump is attributable to the bump, not to the test itself.
 */
class ScheduleSlotDateParsingTest {

    @Test
    fun scheduleSlot_startDate_z_form_resolves_to_expected_epoch_milliseconds() {
        val slot = ScheduleSlot(
            startDate = "2025-10-16T09:00:00Z",
            endDate = "2025-10-16T09:45:00Z"
        )

        assertEquals(1760605200000L, slot.startDateAsEpochMilliseconds)
    }

    @Test
    fun scheduleSlot_endDate_z_form_resolves_to_expected_epoch_milliseconds_and_duration() {
        val slot = ScheduleSlot(
            startDate = "2025-10-16T09:00:00Z",
            endDate = "2025-10-16T09:45:00Z"
        )

        assertEquals(1760607900000L, slot.endDateAsEpochMilliseconds)
        assertEquals(
            45L * 60 * 1000,
            slot.endDateAsEpochMilliseconds - slot.startDateAsEpochMilliseconds
        )
    }

    @Test
    fun scheduleSlot_startDate_utc_offset_form_resolves_to_same_instant_as_z_form() {
        // The backend actually emits this UTC-offset form (see
        // ScheduleSlotDateFormatAndroidTest), not only the `Z` form asserted above.
        val offsetSlot = ScheduleSlot(
            startDate = "2025-10-16T11:00:00+02:00",
            endDate = "2025-10-16T11:45:00+02:00"
        )

        assertEquals(1760605200000L, offsetSlot.startDateAsEpochMilliseconds)
    }

    @Test
    fun scheduleSlot_compareTo_orders_by_start_date_and_treats_self_as_equal() {
        val earlier = ScheduleSlot(startDate = "2025-10-16T09:00:00Z", endDate = "2025-10-16T09:45:00Z")
        val later = ScheduleSlot(startDate = "2025-10-16T09:45:00Z", endDate = "2025-10-16T10:30:00Z")

        assertTrue(earlier < later)
        assertEquals(0, earlier.compareTo(earlier))
    }

    @Test
    fun agenda_day_anchors_resolve_to_expected_epoch_milliseconds() {
        assertEquals(1760572800000L, Instant.parse(Agenda.DAY_ONE_ISO).toEpochMilliseconds())
        assertEquals(1760659200000L, Instant.parse(Agenda.DAY_TWO_ISO).toEpochMilliseconds())
    }

    @Test
    fun agendaBuilder_buckets_sessions_into_day_one_and_day_two() {
        val dayOneSession = Session(
            scheduleSlot = ScheduleSlot(startDate = "2025-10-16T09:00:00Z", endDate = "2025-10-16T09:45:00Z")
        )
        val dayTwoSession = Session(
            scheduleSlot = ScheduleSlot(startDate = "2025-10-17T09:00:00Z", endDate = "2025-10-17T09:45:00Z")
        )

        val agenda = Agenda.Builder().apply {
            sessions = listOf(dayOneSession, dayTwoSession)
        }.build()

        assertEquals(listOf(dayOneSession), agenda.days[1]?.sessions)
        assertEquals(listOf(dayTwoSession), agenda.days[2]?.sessions)
    }

    @Test
    fun agendaBuilder_drops_sessions_outside_the_two_conference_days() {
        // Pinning today's silent-drop behavior so the bump cannot change it unnoticed.
        val outOfRangeSession = Session(
            scheduleSlot = ScheduleSlot(startDate = "2025-10-18T09:00:00Z", endDate = "2025-10-18T09:45:00Z")
        )

        val agenda = Agenda.Builder().apply {
            sessions = listOf(outOfRangeSession)
        }.build()

        assertTrue(agenda.days[1]?.sessions.isNullOrEmpty())
        assertTrue(agenda.days[2]?.sessions.isNullOrEmpty())
    }

    @Test
    fun agendaBuilder_sorts_day_one_sessions_ascending_by_start_date() {
        val later = Session(
            scheduleSlot = ScheduleSlot(startDate = "2025-10-16T11:00:00Z", endDate = "2025-10-16T11:45:00Z")
        )
        val earlier = Session(
            scheduleSlot = ScheduleSlot(startDate = "2025-10-16T09:00:00Z", endDate = "2025-10-16T09:45:00Z")
        )

        val agenda = Agenda.Builder().apply {
            sessions = listOf(later, earlier)
        }.build()

        assertEquals(listOf(earlier, later), agenda.days[1]?.sessions)
    }
}
