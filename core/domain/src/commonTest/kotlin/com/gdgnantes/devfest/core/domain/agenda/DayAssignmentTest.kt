package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/** Day assignment: `UtcDayWindows` against `Agenda.Builder`, `LocalDateDays` in a given zone. */
class DayAssignmentTest {
    private val useCase = GroupSessionsUseCase()
    private val paris = TimeZone.of("Europe/Paris")
    private val auckland = TimeZone.of("Pacific/Auckland")
    private val utcWindows = UtcDayWindows(listOf(Agenda.DAY_ONE_ISO, Agenda.DAY_TWO_ISO))

    private fun sessionAt(id: String, start: String): Session =
        Session(id = id, scheduleSlot = ScheduleSlot(startDate = start, endDate = start))

    private fun List<DaySchedule>.idsByDay(): List<Set<String>> =
        map { day -> day.slots.flatMap { slot -> slot.sessions.map { it.id } }.toSet() }

    private fun builderIdsByDay(sessions: List<Session>): List<Set<String>> {
        val agenda = Agenda.Builder().apply { this.sessions = sessions }.build()
        return listOf(1, 2).map { day -> agenda.days.getValue(day).sessions.map { it.id }.toSet() }
    }

    @Test
    fun utc_day_windows_assign_days_like_agenda_builder_on_the_snapshot() {
        val sessions = AgendaSnapshot.sessions(GoldenFiles.read("agenda/sessions.snapshot.json"))

        assertEquals(builderIdsByDay(sessions), useCase.group(sessions, utcWindows, paris).idsByDay())
    }

    @Test
    fun utc_day_windows_assign_days_like_agenda_builder_on_the_fixtures() {
        val sessions = SessionFixtures.all

        val actual = useCase.group(sessions, utcWindows, paris).idsByDay()

        assertEquals(builderIdsByDay(sessions), actual)
        assertEquals(6, actual[0].size)
        assertEquals(4, actual[1].size)
    }

    @Test
    fun utc_day_windows_ignore_the_time_zone() {
        val sessions = AgendaSnapshot.sessions(GoldenFiles.read("agenda/sessions.snapshot.json"))
        val inParis = useCase.group(sessions, utcWindows, paris)

        assertEquals(inParis, useCase.group(sessions, utcWindows, auckland))
        assertEquals(inParis, useCase.group(sessions, utcWindows, TimeZone.UTC))
    }

    @Test
    fun a_start_at_the_second_day_start_belongs_to_day_two() {
        val days = useCase.group(listOf(sessionAt("s", "2025-10-17T00:00:00Z")), utcWindows, TimeZone.UTC)

        assertEquals(listOf(emptySet(), setOf("s")), days.idsByDay())
    }

    @Test
    fun a_start_one_second_before_the_second_day_belongs_to_day_one() {
        val days = useCase.group(listOf(sessionAt("s", "2025-10-16T23:59:59Z")), utcWindows, TimeZone.UTC)

        assertEquals(listOf(setOf("s"), emptySet()), days.idsByDay())
    }

    @Test
    fun a_start_a_day_before_the_first_day_is_dropped() {
        val days = useCase.group(listOf(sessionAt("s", "2025-10-15T00:00:00Z")), utcWindows, TimeZone.UTC)

        assertEquals(listOf(emptySet(), emptySet()), days.idsByDay())
    }

    @Test
    fun a_start_after_the_last_window_is_dropped() {
        val days = useCase.group(listOf(sessionAt("s", "2025-10-18T00:00:00Z")), utcWindows, TimeZone.UTC)

        assertEquals(listOf(emptySet(), emptySet()), days.idsByDay())
    }

    @Test
    fun an_unparseable_day_start_yields_that_day_without_slots_and_does_not_throw() {
        val assignment = UtcDayWindows(listOf("not-a-date", Agenda.DAY_TWO_ISO))
        val sessions = listOf(sessionAt("one", "2025-10-14T08:00:00Z"), sessionAt("two", "2025-10-17T08:00:00Z"))

        val days = useCase.group(sessions, assignment, TimeZone.UTC)

        assertEquals(listOf("not-a-date", Agenda.DAY_TWO_ISO), days.map { it.date })
        assertEquals(listOf(emptySet(), setOf("two")), days.idsByDay())
    }

    @Test
    fun day_positions_are_zero_based_and_null_outside_every_day() {
        val start = Instant.parse("2025-10-17T08:00:00Z")

        assertEquals(1, utcWindows.dayPosition(start, TimeZone.UTC))
        assertNull(UtcDayWindows(emptyList()).dayPosition(start, TimeZone.UTC))
        assertNull(LocalDateDays(listOf("2025-10-16")).dayPosition(start, TimeZone.UTC))
    }

    @Test
    fun local_date_days_follow_the_time_zone_passed_to_group() {
        val assignment = LocalDateDays(listOf("2025-10-16", "2025-10-17"))
        val sessions = listOf(sessionAt("evening", "2025-10-16T18:30:00+02:00"))

        assertEquals(
            listOf(setOf("evening"), emptySet()),
            useCase.group(sessions, assignment, paris).idsByDay()
        )
        assertEquals(
            listOf(emptySet(), setOf("evening")),
            useCase.group(sessions, assignment, auckland).idsByDay()
        )
    }

    @Test
    fun local_date_days_emit_every_configured_day_with_its_label() {
        val assignment = LocalDateDays(listOf("2025-10-16", "2025-10-17"))

        val days = useCase.group(emptyList(), assignment, paris)

        assertEquals(listOf(1, 2), days.map { it.dayIndex })
        assertEquals(listOf("2025-10-16", "2025-10-17"), days.map { it.date })
    }
}
