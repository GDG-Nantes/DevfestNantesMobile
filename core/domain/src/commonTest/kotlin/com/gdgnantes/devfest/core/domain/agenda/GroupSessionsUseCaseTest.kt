package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.testing.snapshot.AgendaGolden
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import com.gdgnantes.devfest.core.testing.snapshot.Goldens
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/** Day and time-slot grouping against the v1.0 Android and iOS goldens and the edge rules. */
class GroupSessionsUseCaseTest {
    private val useCase = GroupSessionsUseCase()
    private val utcWindows = UtcDayWindows(listOf(Agenda.DAY_ONE_ISO, Agenda.DAY_TWO_ISO))
    private val localDays =
        LocalDateDays(listOf(Agenda.DAY_ONE_ISO.substring(0, DATE_LENGTH), Agenda.DAY_TWO_ISO.substring(0, DATE_LENGTH)))
    private val snapshot: List<Session> by lazy {
        AgendaSnapshot.sessions(GoldenFiles.read("agenda/sessions.snapshot.json"))
    }

    private fun room(sortIndex: Int, name: String) = Room(id = name, name = name, sortIndex = sortIndex)

    private fun sessionAt(id: String, start: String, room: Room? = null): Session =
        Session(id = id, room = room, scheduleSlot = ScheduleSlot(startDate = start, endDate = start))

    private val androidCanonical: Comparator<Session> =
        compareBy({ it.room?.sortIndex ?: -1 }, { it.room?.name.orEmpty() }, { it.id })

    private val iosCanonical: Comparator<Session> = compareBy({ it.room?.name.orEmpty() }, { it.id })

    private val roomOrder: Comparator<Session> = compareBy({ it.room?.sortIndex ?: -1 }, { it.room?.name.orEmpty() })

    private fun List<DaySchedule>.idsPerSlot(order: Comparator<Session>): List<List<List<String>>> =
        map { day -> day.slots.map { slot -> slot.sessions.sortedWith(order).map { it.id } } }

    private fun golden(path: String): AgendaGolden = Goldens.decodeAgenda(GoldenFiles.read(path))

    private fun List<DaySchedule>.slotStarts(): List<List<String>> = map { day -> day.slots.map { it.start.toString() } }

    private fun List<DaySchedule>.slotMembers(): List<List<Set<String>>> =
        map { day -> day.slots.map { slot -> slot.sessions.map { it.id }.toSet() } }

    private fun AgendaGolden.slotStarts(): List<List<String>> = days.map { day -> day.slots.map { it.start } }

    private fun AgendaGolden.slotMembers(): List<List<Set<String>>> =
        days.map { day -> day.slots.map { it.sessionIds.toSet() } }

    @Test
    fun the_android_golden_is_reproduced_for_every_time_zone() {
        val golden = golden("agenda/golden/android-agenda.json")

        for (zone in listOf("Europe/Paris", "Pacific/Auckland", "UTC")) {
            val days = useCase.group(snapshot, utcWindows, TimeZone.of(zone))

            assertEquals(golden.days.map { it.dayIndex }, days.map { it.dayIndex }, "day indexes in $zone")
            assertEquals(golden.days.map { it.date }, days.map { it.date }, "day dates in $zone")
            assertEquals(golden.slotStarts(), days.slotStarts(), "slot starts in $zone")
            assertEquals(
                golden.days.map { day -> day.slots.map { it.sessionIds } },
                days.idsPerSlot(androidCanonical),
                "slot ids in $zone"
            )
        }
    }

    @Test
    fun the_in_slot_order_follows_the_room_sort_index_then_the_room_name() {
        val days = useCase.group(snapshot, utcWindows, TimeZone.UTC)

        for (slot in days.flatMap { it.slots }) {
            assertEquals(
                slot.sessions.map { it.id }.toSet(),
                slot.sessions.sortedWith(roomOrder).map { it.id }.toSet()
            )
            assertTrue(
                slot.sessions.zipWithNext().all { (a, b) -> roomOrder.compare(a, b) <= 0 },
                "slot ${slot.start} is not in room order"
            )
        }
    }

    @Test
    fun slots_are_in_chronological_order() {
        for (day in useCase.group(snapshot, utcWindows, TimeZone.UTC)) {
            val starts = day.slots.map { it.start }
            assertEquals(starts.sorted().distinct(), starts, "day ${day.dayIndex}")
        }
    }

    @Test
    fun the_ios_goldens_are_reproduced_with_the_documented_room_order_deviation() {
        for (zone in listOf("Europe/Paris", "Pacific/Auckland")) {
            val golden = golden("agenda/golden/ios-agenda-${zone.replace('/', '_')}.json")

            val days = useCase.group(snapshot, localDays, TimeZone.of(zone))

            assertEquals(golden.days.map { it.dayIndex }, days.map { it.dayIndex }, "day indexes in $zone")
            assertEquals(golden.days.map { it.date }, days.map { it.date }, "day dates in $zone")
            assertEquals(golden.slotStarts(), days.slotStarts(), "slot starts in $zone")
            assertEquals(golden.slotMembers(), days.slotMembers(), "slot membership in $zone")
            // Same slot content as iOS v1.0; only the order inside a slot differs (documented
            // deviation): canonicalized like iOS (room name, then id) it equals the golden order.
            assertEquals(
                golden.days.map { day -> day.slots.map { it.sessionIds } },
                days.idsPerSlot(iosCanonical),
                "ios canonical order in $zone"
            )
            assertTrue(
                days.flatMap { it.slots }.all { slot ->
                    slot.sessions.zipWithNext().all { (a, b) -> roomOrder.compare(a, b) <= 0 }
                },
                "the shared in-slot order is the room sort index order in $zone"
            )
        }
    }

    @Test
    fun the_ios_day_assignment_differs_between_zones_on_the_same_snapshot() {
        val inParis = useCase.group(snapshot, localDays, TimeZone.of("Europe/Paris")).slotMembers()
        val inAuckland = useCase.group(snapshot, localDays, TimeZone.of("Pacific/Auckland")).slotMembers()

        assertTrue(inParis != inAuckland)
    }

    @Test
    fun two_sessions_with_the_same_start_share_one_slot() {
        val sessions = listOf(sessionAt("a", "2025-10-16T10:00:00Z"), sessionAt("b", "2025-10-16T10:00:00+00:00"))

        val slots = useCase.group(sessions, utcWindows, TimeZone.UTC).first().slots

        assertEquals(1, slots.size)
        assertEquals(listOf("a", "b"), slots.single().sessions.map { it.id })
    }

    @Test
    fun starts_one_second_apart_give_two_slots() {
        val sessions = listOf(sessionAt("a", "2025-10-16T10:00:01Z"), sessionAt("b", "2025-10-16T10:00:00Z"))

        val slots = useCase.group(sessions, utcWindows, TimeZone.UTC).first().slots

        assertEquals(
            listOf(Instant.parse("2025-10-16T10:00:00Z"), Instant.parse("2025-10-16T10:00:01Z")),
            slots.map { it.start }
        )
        assertEquals(listOf("b", "a"), slots.flatMap { slot -> slot.sessions.map { it.id } })
    }

    @Test
    fun an_empty_session_list_gives_every_configured_day_without_slots() {
        val days = useCase.group(emptyList(), utcWindows, TimeZone.UTC)

        assertEquals(listOf(1, 2), days.map { it.dayIndex })
        assertEquals(listOf(Agenda.DAY_ONE_ISO, Agenda.DAY_TWO_ISO), days.map { it.date })
        assertTrue(days.all { it.slots.isEmpty() })
    }

    @Test
    fun a_single_session_gives_one_day_with_one_slot() {
        val days = useCase.group(listOf(sessionAt("a", "2025-10-17T09:00:00Z")), utcWindows, TimeZone.UTC)

        assertTrue(days[0].slots.isEmpty())
        assertEquals(1, days[1].slots.size)
        assertEquals(listOf("a"), days[1].slots.single().sessions.map { it.id })
    }

    @Test
    fun a_configured_day_without_session_is_still_present() {
        val days = useCase.group(listOf(sessionAt("a", "2025-10-16T09:00:00Z")), utcWindows, TimeZone.UTC)

        assertEquals(2, days.size)
        assertTrue(days[1].slots.isEmpty())
    }

    @Test
    fun a_slot_is_ordered_by_room_with_a_roomless_session_first() {
        val start = "2025-10-16T09:00:00Z"
        val sessions =
            listOf(
                sessionAt("unknown-room", start, room(Int.MAX_VALUE, "Z")),
                sessionAt("second", start, room(2, "B")),
                sessionAt("none", start),
                sessionAt("first", start, room(1, "C"))
            )

        val slot = useCase.group(sessions, utcWindows, TimeZone.UTC).first().slots.single()

        assertEquals(listOf("none", "first", "second", "unknown-room"), slot.sessions.map { it.id })
    }

    @Test
    fun sessions_with_equal_room_keys_keep_their_input_order() {
        val start = "2025-10-16T09:00:00Z"
        val shared = room(3, "Titan")
        val sessions = listOf("c", "a", "d", "b").map { sessionAt(it, start, shared) }

        val slot = useCase.group(sessions, utcWindows, TimeZone.UTC).first().slots.single()

        assertEquals(listOf("c", "a", "d", "b"), slot.sessions.map { it.id })
    }

    private companion object {
        const val DATE_LENGTH = 10
        const val PAD = 11
    }
}
