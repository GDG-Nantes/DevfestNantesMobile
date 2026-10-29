package com.gdgnantes.devfest.feature.agenda.characterization

import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.FilterCase
import com.gdgnantes.devfest.core.testing.snapshot.FilterCases
import com.gdgnantes.devfest.core.testing.snapshot.FilteringGolden
import com.gdgnantes.devfest.core.testing.snapshot.GoldenCase
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import com.gdgnantes.devfest.core.testing.snapshot.Goldens
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.agenda.FakeSessionFiltersService
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * D-11 characterization of the v1.0 Android filtering, written against the unchanged code: the real
 * [AgendaViewModel] runs every case of `agenda/filter-cases.json` on the recorded snapshot, with the
 * shared bookmark set, and the resulting session ids (both days) become
 * `golden/android-filtering.json`, the oracle the shared filter use case must reproduce.
 *
 * The VM keeps both day keys whatever the filters leave, so an empty result still exposes days 1 and
 * 2 with no session. Ids are sorted ascending in the golden, because the v1.0 order inside a day
 * comes from HashSet iteration among equal start dates.
 */
class AgendaFilteringCharacterizationTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val cases: FilterCases = FilterCases.decode(GoldenFiles.read("agenda/filter-cases.json"))

    private val sessions =
        AgendaSnapshot.sessions(GoldenFiles.read("agenda/sessions.snapshot.json"))

    private fun viewModelFor(case: FilterCase): AgendaViewModel {
        val bookmarks = FakeBookmarksStore()
        cases.bookmarkedSessionIds.forEach { bookmarks.setBookmarked(it, true) }
        val filters =
            case.filters.map { SessionFilter(SessionFilterType.valueOf(it.type), it.value) }.toSet()

        return AgendaViewModel(
            FakeDevFestNantesStore(sessions = sessions),
            bookmarks,
            NoOpPerformanceMonitoring(),
            FakeSessionFiltersService(filters),
            mainDispatcherRule.dispatcher,
        )
    }

    private fun AgendaViewModel.allIds(): List<String> =
        days.value.values.flatMap { day -> day.sessions.map { it.id } }.sorted()

    @Test
    fun every_shared_filter_case_matches_the_golden_and_keeps_both_days() {
        val golden =
            FilteringGolden(
                source = "android-v1.0",
                cases =
                    cases.cases.map { case ->
                        val viewModel = viewModelFor(case)

                        val dayKeys = viewModel.days.value.keys
                        assertEquals(setOf(1, 2), dayKeys, "${case.name}: both days must stay present")

                        val ids = viewModel.allIds()
                        assertEquals(ids.distinct(), ids, "${case.name}: a session is listed twice")
                        GoldenCase(name = case.name, sessionIds = ids)
                    },
            )

        assertEquals(15, golden.cases.size)
        GoldenFiles.assertMatches("agenda/golden/android-filtering.json", Goldens.encode(golden))
    }

    @Test
    fun an_unfiltered_agenda_lists_every_snapshot_session_in_a_day_window() {
        val noFilter = cases.cases.first { it.name == "no_filter" }

        val ids = viewModelFor(noFilter).allIds()

        assertEquals(sessions.map { it.id }.sorted(), ids)
    }

    @Test
    fun the_room_filter_keeps_only_sessions_of_that_room() {
        val titan = cases.cases.first { it.name == "room_titan" }

        val viewModel = viewModelFor(titan)
        val rooms =
            viewModel.days.value.values.flatMap { day -> day.sessions.map { it.room?.id } }.toSet()

        assertEquals(setOf("Titan"), rooms)
    }

    @Test
    fun a_case_without_any_matching_session_still_exposes_days_one_and_two_with_no_session() {
        val viewModel =
            AgendaViewModel(
                FakeDevFestNantesStore(sessions = sessions),
                FakeBookmarksStore(),
                NoOpPerformanceMonitoring(),
                FakeSessionFiltersService(
                    setOf(
                        SessionFilter(SessionFilterType.ROOM, "no such room"),
                    ),
                ),
                mainDispatcherRule.dispatcher,
            )

        assertEquals(setOf(1, 2), viewModel.days.value.keys)
        assertTrue(viewModel.days.value.values.all { it.sessions.isEmpty() })
    }
}
