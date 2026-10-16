package com.gdgnantes.devfest.feature.agenda

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.AgendaDay
import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.model.stubs.roomStubs
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.ui.UiState
import com.gdgnantes.devfest.feature.agenda.utils.SessionFilter
import com.gdgnantes.devfest.feature.agenda.utils.SessionFilter.FilterType
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Specification of the agenda filtering rules, written against the named session fixtures
 * (see the derived facts in `SessionFixtures`). Expected ids are always fixture constants.
 */
class AgendaViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val store = FakeDevFestNantesStore()
    private val bookmarks = FakeBookmarksStore()
    private val filters = FakeSessionFiltersService()

    private fun viewModel() =
        AgendaViewModel(
            store,
            bookmarks,
            NoOpPerformanceMonitoring(),
            filters,
            mainDispatcherRule.dispatcher
        )

    private fun AgendaViewModel.ids(day: Int): Set<String> =
        days.value.getValue(day).sessions.map { it.id }.toSet()

    private fun AgendaViewModel.allIds(): Set<String> = ids(1) + ids(2)

    private fun idsOf(vararg sessions: Session): Set<String> =
        sessions.map { it.id }.toSet()

    private val french = SessionFilter(FilterType.LANGUAGE, SessionLanguage.FRENCH.name)
    private val english = SessionFilter(FilterType.LANGUAGE, SessionLanguage.ENGLISH.name)
    private val advanced = SessionFilter(FilterType.COMPLEXITY, Complexity.ADVANCED.name)
    private val beginner = SessionFilter(FilterType.COMPLEXITY, Complexity.BEGINNER.name)
    private val conference = SessionFilter(FilterType.TYPE, SessionType.CONFERENCE.name)
    private val codelab = SessionFilter(FilterType.TYPE, SessionType.CODELAB.name)
    private val bookmarkedOnly = SessionFilter(FilterType.BOOKMARK, "")
    private val titan = SessionFilter(FilterType.ROOM, requireNotNull(SessionFixtures.composeDeepDive.room).id)
    private val belem = SessionFilter(FilterType.ROOM, requireNotNull(SessionFixtures.webRendering.room).id)

    @Test
    fun loads_the_fixture_agenda_into_two_days_and_reports_success() {
        val viewModel = viewModel()

        assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        assertEquals(
            idsOf(
                SessionFixtures.welcome,
                SessionFixtures.composeDeepDive,
                SessionFixtures.webRendering,
                SessionFixtures.kubernetesLab,
                SessionFixtures.lunch,
                SessionFixtures.gitQuickie
            ),
            viewModel.ids(1)
        )
        assertEquals(
            idsOf(
                SessionFixtures.aiKeynote,
                SessionFixtures.securityTalk,
                SessionFixtures.coffeeBreak,
                SessionFixtures.closing
            ),
            viewModel.ids(2)
        )
    }

    @Test
    fun no_filter_keeps_every_session() {
        val viewModel = viewModel()

        assertEquals(SessionFixtures.all.map { it.id }.toSet(), viewModel.allIds())
    }

    @Test
    fun french_filter_keeps_the_four_french_sessions() {
        filters.setFilters(setOf(french))

        assertEquals(
            idsOf(
                SessionFixtures.welcome,
                SessionFixtures.webRendering,
                SessionFixtures.gitQuickie,
                SessionFixtures.closing
            ),
            viewModel().allIds()
        )
    }

    @Test
    fun beginner_filter_keeps_kubernetes_lab_and_closing() {
        filters.setFilters(setOf(beginner))

        assertEquals(idsOf(SessionFixtures.kubernetesLab, SessionFixtures.closing), viewModel().allIds())
    }

    @Test
    fun conference_type_filter_excludes_the_session_without_type() {
        filters.setFilters(setOf(conference))

        val ids = viewModel().allIds()

        assertEquals(
            idsOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering, SessionFixtures.securityTalk),
            ids
        )
        assertTrue(SessionFixtures.closing.id !in ids)
    }

    @Test
    fun titan_room_filter_keeps_compose_deep_dive_and_closing() {
        filters.setFilters(setOf(titan))

        assertEquals(idsOf(SessionFixtures.composeDeepDive, SessionFixtures.closing), viewModel().allIds())
    }

    @Test
    fun two_room_filters_are_ored() {
        filters.setFilters(setOf(titan, belem))

        assertEquals(
            idsOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering, SessionFixtures.closing),
            viewModel().allIds()
        )
    }

    @Test
    fun two_language_filters_are_ored() {
        filters.setFilters(setOf(french, english))

        assertEquals(
            SessionFixtures.all.filter { it.language != null }.map { it.id }.toSet(),
            viewModel().allIds()
        )
    }

    @Test
    fun french_and_advanced_returns_only_les_secrets_du_rendu_web() {
        filters.setFilters(setOf(french, advanced))

        assertEquals(idsOf(SessionFixtures.webRendering), viewModel().allIds())
    }

    @Test
    fun codelab_and_french_returns_nothing() {
        filters.setFilters(setOf(codelab, french))

        val viewModel = viewModel()

        assertEquals(setOf(1, 2), viewModel.days.value.keys)
        assertEquals(emptySet(), viewModel.ids(1))
        assertEquals(emptySet(), viewModel.ids(2))
    }

    @Test
    fun bookmark_filter_keeps_only_bookmarked_sessions() {
        bookmarks.setBookmarked(SessionFixtures.lunch.id, true)
        bookmarks.setBookmarked(SessionFixtures.closing.id, true)
        filters.setFilters(setOf(bookmarkedOnly))

        val viewModel = viewModel()

        assertEquals(idsOf(SessionFixtures.lunch, SessionFixtures.closing), viewModel.allIds())

        bookmarks.setBookmarked(SessionFixtures.welcome.id, true)

        assertEquals(
            idsOf(SessionFixtures.welcome, SessionFixtures.lunch, SessionFixtures.closing),
            viewModel.allIds()
        )
        assertEquals(setOf(bookmarkedOnly), filters.filters.value)
    }

    @Test
    fun unbookmarking_removes_the_session_under_a_bookmark_filter() {
        bookmarks.setBookmarked(SessionFixtures.lunch.id, true)
        bookmarks.setBookmarked(SessionFixtures.closing.id, true)
        filters.setFilters(setOf(bookmarkedOnly))
        val viewModel = viewModel()

        bookmarks.setBookmarked(SessionFixtures.lunch.id, false)

        assertEquals(idsOf(SessionFixtures.closing), viewModel.allIds())
    }

    @Test
    fun bookmarks_do_not_affect_the_agenda_without_a_bookmark_filter() {
        val viewModel = viewModel()

        bookmarks.setBookmarked(SessionFixtures.lunch.id, true)

        assertEquals(SessionFixtures.all.map { it.id }.toSet(), viewModel.allIds())
    }

    @Test
    fun language_filter_never_matches_a_session_without_language() {
        val viewModel = viewModel()

        for (language in listOf(french, english)) {
            filters.setFilters(setOf(language))

            val ids = viewModel.allIds()
            assertTrue(SessionFixtures.lunch.id !in ids, "lunch matched ${language.value}")
            assertTrue(SessionFixtures.coffeeBreak.id !in ids, "coffee break matched ${language.value}")
        }
    }

    @Test
    fun sessions_within_each_day_are_sorted_by_start_date() {
        val viewModel = viewModel()

        for (day in listOf(1, 2)) {
            val startDates = viewModel.days.value.getValue(day).sessions.map { it.scheduleSlot.startDate }
            assertTrue(
                startDates.zipWithNext().all { (a, b) -> a <= b },
                "day $day is not sorted: $startDates"
            )
        }
        // Sessions sharing a start time come out in an unspecified order (HashSet iteration),
        // so they are compared as a set.
        val tenOClock =
            viewModel.days.value.getValue(1).sessions
                .filter { it.scheduleSlot.startDate == SessionFixtures.webRendering.scheduleSlot.startDate }
        assertEquals(
            idsOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering, SessionFixtures.kubernetesLab),
            tenOClock.map { it.id }.toSet()
        )
    }

    @Test
    fun filter_change_emits_the_filtered_days() =
        runTest {
            val viewModel = viewModel()

            viewModel.days.test {
                assertEquals(SessionFixtures.all.map { it.id }.toSet(), awaitItem().ids())

                filters.setFilters(setOf(english))

                assertEquals(
                    idsOf(
                        SessionFixtures.composeDeepDive,
                        SessionFixtures.kubernetesLab,
                        SessionFixtures.aiKeynote,
                        SessionFixtures.securityTalk
                    ),
                    awaitItem().ids()
                )
            }
        }

    @Test
    fun onSessionFiltersChanged_forwards_to_the_service() {
        val viewModel = viewModel()
        val requested = setOf(french, advanced)

        viewModel.onSessionFiltersChanged(requested)

        assertEquals(requested, filters.filters.value)
        assertEquals(requested, viewModel.sessionFilters.value)
        assertEquals(idsOf(SessionFixtures.webRendering), viewModel.allIds())
    }

    @Test
    fun new_store_sessions_replace_the_agenda() {
        val viewModel = viewModel()

        store.sessionsState.value = listOf(SessionFixtures.welcome)

        assertEquals(idsOf(SessionFixtures.welcome), viewModel.ids(1))
        assertEquals(emptySet(), viewModel.ids(2))
    }

    @Test
    fun on_refresh_keeps_the_loaded_agenda() {
        filters.setFilters(setOf(french))
        val viewModel = viewModel()
        val before = viewModel.days.value

        viewModel.onRefresh()

        assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        assertEquals(before, viewModel.days.value)
    }

    // Characterizes a production defect (deferred, D-10): AgendaViewModel never catches a store
    // failure. The error escapes viewModelScope uncaught (the app would crash), uiState never
    // leaves STARTING and the days stay empty.
    @Test
    fun store_failure_is_characterized() {
        store.failure = IllegalStateException("boom")

        val thrown =
            assertFailsWith<IllegalStateException> {
                runTest {
                    val viewModel = viewModel()

                    assertEquals(UiState.STARTING, viewModel.uiState.value)
                    assertEquals(emptySet(), viewModel.allIds())
                }
            }

        assertEquals("boom", thrown.message)
    }

    private fun Map<Int, AgendaDay>.ids(): Set<String> = values.flatMap { it.sessions }.map { it.id }.toSet()
}

/** Needs to observe the state before the init coroutines run, hence a standard dispatcher. */
class AgendaViewModelStartupTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeDevFestNantesStore()

    private fun viewModel() =
        AgendaViewModel(
            store,
            FakeBookmarksStore(),
            NoOpPerformanceMonitoring(),
            FakeSessionFiltersService(),
            mainDispatcherRule.dispatcher
        )

    @Test
    fun ui_state_goes_from_starting_to_success() =
        runTest {
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(UiState.STARTING, awaitItem())
                assertEquals(UiState.SUCCESS, awaitItem())
            }
        }

    @Test
    fun rooms_exposes_the_store_rooms() =
        runTest {
            val viewModel = viewModel()

            viewModel.rooms.test {
                assertEquals(emptySet(), awaitItem())
                assertEquals(roomStubs.toSet(), awaitItem())

                store.roomsState.value = setOf(roomStubs.first())

                assertEquals(setOf(roomStubs.first()), awaitItem())
            }
        }
}
