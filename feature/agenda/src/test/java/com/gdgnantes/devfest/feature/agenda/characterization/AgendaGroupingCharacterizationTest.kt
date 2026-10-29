package com.gdgnantes.devfest.feature.agenda.characterization

import com.gdgnantes.devfest.core.domain.agenda.FilterSessionsUseCase
import com.gdgnantes.devfest.core.domain.agenda.GroupSessionsUseCase
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.FakeSessionFiltersStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.testing.snapshot.AgendaGolden
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.GoldenDay
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import com.gdgnantes.devfest.core.testing.snapshot.GoldenSlot
import com.gdgnantes.devfest.core.testing.snapshot.Goldens
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.agenda.formatSlotTime
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Locale
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * D-11 characterization of the v1.0 Android agenda grouping, written against the unchanged code:
 * the real [AgendaViewModel] output goes through the v1.0 grouping and the LazyColumn re-sort of
 * `AgendaColumn`, on the recorded snapshot, under two time zones and two locales. Since 07-15 the
 * grouping is the shared one: the selector rebuilds the label-keyed groups from the emitted slots.
 *
 * It proves D-06 (label keys give the same slots as start-instant keys on the snapshot) and writes
 * `golden/android-agenda.json`, the oracle the shared grouping must reproduce. The v1.0 order among
 * equal sort keys comes from HashSet iteration, so the golden canonicalizes ties by id.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AgendaGroupingCharacterizationTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var savedTimeZone: TimeZone
    private lateinit var savedLocale: Locale

    @Before
    fun setUp() {
        savedTimeZone = TimeZone.getDefault()
        savedLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(savedTimeZone)
        Locale.setDefault(savedLocale)
    }

    /** One displayed day: groups as `AgendaColumn` lists them (label, rows in displayed order). */
    private class DisplayedDay(
        val dayIndex: Int,
        val date: String,
        val inputSize: Int,
        val groups: List<Pair<String, List<Session>>>,
    )

    private fun useEnvironment(zoneId: String, locale: Locale, qualifiers: String) {
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
        Locale.setDefault(locale)
        RuntimeEnvironment.setQualifiers(qualifiers)
    }

    private fun snapshotSessions(): List<Session> =
        AgendaSnapshot.sessions(GoldenFiles.read("agenda/sessions.snapshot.json"))

    private fun displayedDays(sessions: List<Session>): List<DisplayedDay> {
        val store = FakeDevFestNantesStore(sessions = sessions)
        val filters = FakeSessionFiltersStore()
        val viewModel =
            AgendaViewModel(
                ObserveAgendaUseCase(
                    store,
                    filters,
                    FakeBookmarksStore(),
                    FilterSessionsUseCase(),
                    GroupSessionsUseCase(),
                    mainDispatcherRule.dispatcher,
                ),
                store,
                filters,
                NoOpPerformanceMonitoring(),
            )
        return viewModel.days.value.sortedBy { it.dayIndex }.map { day ->
            DisplayedDay(
                dayIndex = day.dayIndex,
                date = day.date,
                inputSize = day.slots.sumOf { it.sessions.size },
                groups =
                    day.slots.map { slot ->
                        formatSlotTime(slot.start) to slot.sessions.sortedBy { session -> session.room?.sortIndex }
                    },
            )
        }
    }

    /** The label-keyed groups of v1.0, rebuilt from the slots the ViewModel emits for [sessions]. */
    private fun labelGroups(sessions: List<Session>): Map<String, List<Session>> =
        displayedDays(sessions).flatMap { day -> day.groups }.toMap()

    private fun Session.startInstant(): Instant = Instant.parse(scheduleSlot.startDate)

    private val displayOrder =
        compareBy<Session>({ it.room?.sortIndex }, { it.room?.name })

    private val canonicalOrder =
        compareBy<Session>({ it.room?.sortIndex }, { it.room?.name }, { it.id })

    private fun assertLabelKeysAreStartInstantKeys(days: List<DisplayedDay>) {
        days.forEach { day ->
            val label = "day ${day.dayIndex}"
            val groupStarts =
                day.groups.map { (_, sessions) ->
                    val starts = sessions.map { it.startInstant() }.distinct()
                    assertEquals(1, starts.size, "$label: a label group mixes start instants $starts")
                    starts.single()
                }
            assertEquals(groupStarts.size, groupStarts.distinct().size, "$label: two labels share a start instant")
            assertEquals(groupStarts.sorted(), groupStarts, "$label: groups are not chronological")
            assertEquals(
                day.inputSize,
                day.groups.sumOf { it.second.size },
                "$label: a session was dropped or duplicated",
            )
        }
    }

    private fun assertDisplayedOrder(days: List<DisplayedDay>) {
        days.forEach { day ->
            day.groups.forEach { (label, sessions) ->
                sessions.zipWithNext().forEach { (a, b) ->
                    assertTrue(
                        displayOrder.compare(a, b) <= 0,
                        "day ${day.dayIndex} $label: ${a.id} (${a.room}) is displayed before ${b.id} (${b.room})",
                    )
                }
            }
        }
    }

    private fun goldenOf(days: List<DisplayedDay>): AgendaGolden =
        AgendaGolden(
            source = "android-v1.0",
            timeZone = "UTC",
            days =
                days.map { day ->
                    GoldenDay(
                        dayIndex = day.dayIndex,
                        date = day.date,
                        slots =
                            day.groups.map { (_, sessions) ->
                                GoldenSlot(
                                    start = sessions.first().startInstant().toString(),
                                    sessionIds = sessions.sortedWith(canonicalOrder).map { it.id },
                                )
                            },
                    )
                },
        )

    @Test
    fun the_snapshot_grouping_is_the_same_in_both_zones_and_locales_and_matches_the_golden() {
        val sessions = snapshotSessions()

        useEnvironment("Europe/Paris", Locale.forLanguageTag("fr-FR"), "fr-rFR")
        val parisDays = displayedDays(sessions)
        useEnvironment("Pacific/Auckland", Locale.forLanguageTag("en-US"), "en-rUS")
        val aucklandDays = displayedDays(sessions)

        assertEquals(2, parisDays.size)
        listOf(parisDays, aucklandDays).forEach {
            assertLabelKeysAreStartInstantKeys(it)
            assertDisplayedOrder(it)
        }

        val labelsPerSlot =
            parisDays.zip(aucklandDays).flatMap { (paris, auckland) ->
                paris.groups.map { it.first }.zip(auckland.groups.map { it.first })
            }
        assertTrue(labelsPerSlot.isNotEmpty())
        assertTrue(
            labelsPerSlot.all { (fr, en) -> fr != en },
            "fr-FR and en-US labels of the same slot must differ, so the locale is applied: $labelsPerSlot",
        )

        val golden = goldenOf(parisDays)
        assertEquals(golden, goldenOf(aucklandDays), "slots must not depend on the time zone or the locale")

        val inDayWindows =
            sessions.count { session ->
                (session.startInstant() - Agenda.DAY_ONE).inWholeDays == 0L ||
                    (session.startInstant() - Agenda.DAY_TWO).inWholeDays == 0L
            }
        assertEquals(inDayWindows, golden.days.sumOf { day -> day.slots.sumOf { it.sessionIds.size } })
        println("android-agenda golden: $inDayWindows of ${sessions.size} snapshot sessions fall in a day window")

        GoldenFiles.assertMatches("agenda/golden/android-agenda.json", Goldens.encode(golden))
    }

    @Test
    fun a_roomless_session_is_listed_first_in_its_slot() {
        useEnvironment("Europe/Paris", Locale.forLanguageTag("fr-FR"), "fr-rFR")
        val start = "2025-10-16T10:00:00+02:00"
        val end = "2025-10-16T10:50:00+02:00"

        fun session(id: String, room: Room?) =
            Session(id = id, title = id, room = room, scheduleSlot = ScheduleSlot(endDate = end, startDate = start))

        val days =
            displayedDays(
                listOf(
                    session("belem", Room("Belem", "Belem", 2)),
                    session("titan", Room("Titan", "Titan", 1)),
                    session("roomless", null),
                ),
            )

        val slot = days.first().groups.single().second
        assertEquals(listOf("roomless", "titan", "belem"), slot.map { it.id })
        assertDisplayedOrder(days)
    }

    @Test
    fun equal_starts_merge_and_distinct_starts_never_share_a_label() {
        useEnvironment("Europe/Paris", Locale.forLanguageTag("fr-FR"), "fr-rFR")

        fun session(id: String, start: String) =
            Session(id = id, title = id, scheduleSlot = ScheduleSlot(endDate = start, startDate = start))

        val grouped =
            labelGroups(
                listOf(
                    session("a", "2025-10-16T10:00:00+02:00"),
                    session("b", "2025-10-16T10:00:00+02:00"),
                    session("c", "2025-10-16T10:05:00+02:00"),
                    session("d", "2025-10-16T09:59:00+02:00"),
                ),
            )

        assertEquals(3, grouped.size)
        assertEquals(setOf("a", "b"), grouped.values.first { it.size == 2 }.map { it.id }.toSet())
        assertEquals(
            setOf("c", "d"),
            grouped.values.filter { it.size == 1 }.map { it.single().id }.toSet(),
        )
    }
}
