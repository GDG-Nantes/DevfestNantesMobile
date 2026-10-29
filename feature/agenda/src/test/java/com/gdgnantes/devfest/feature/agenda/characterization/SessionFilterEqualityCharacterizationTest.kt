package com.gdgnantes.devfest.feature.agenda.characterization

import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.agenda.FakeSessionFiltersService
import com.gdgnantes.devfest.feature.agenda.utils.SessionFilter
import com.gdgnantes.devfest.feature.agenda.utils.SessionFilter.FilterType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * D-11 / D-14 characterization of equal-content filter sets on the v1.0 [AgendaViewModel], where
 * `SessionFilter` is a plain class with identity equality: applying a set of new instances with the
 * same `(type, value)` pairs leaves the visible agenda and the active filters unchanged.
 *
 * The only difference expected after D-14 (`SessionFilter` becomes a data class) is internal: an
 * equal-content set no longer re-emits through the `StateFlow` and the agenda is not recomputed.
 * Nothing visible depends on it (RESEARCH A7), so these assertions stay valid unchanged.
 */
class SessionFilterEqualityCharacterizationTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val filtersService = FakeSessionFiltersService()

    // Built per test: viewModelScope needs Dispatchers.Main, which the rule installs after construction.
    private val viewModel by lazy {
        AgendaViewModel(
            FakeDevFestNantesStore(),
            FakeBookmarksStore(),
            NoOpPerformanceMonitoring(),
            filtersService,
            mainDispatcherRule.dispatcher,
        )
    }

    private fun AgendaViewModel.allIds(): Set<String> =
        days.value.values.flatMap { day -> day.sessions.map { it.id } }.toSet()

    private fun AgendaViewModel.filterPairs(): Set<Pair<FilterType, String>> =
        sessionFilters.value.map { it.type to it.value }.toSet()

    private fun french() = setOf(SessionFilter(FilterType.LANGUAGE, SessionLanguage.FRENCH.name))

    @Test
    fun reapplying_an_equal_content_filter_set_leaves_the_agenda_and_the_filters_unchanged() {
        val unfilteredIds = viewModel.allIds()

        viewModel.onSessionFiltersChanged(french())
        val idsAfterFirst = viewModel.allIds()
        val pairsAfterFirst = viewModel.filterPairs()

        viewModel.onSessionFiltersChanged(french())

        assertTrue(idsAfterFirst.size < unfilteredIds.size, "the french filter must narrow the agenda")
        assertEquals(idsAfterFirst, viewModel.allIds())
        assertEquals(pairsAfterFirst, viewModel.filterPairs())
        assertEquals(setOf(FilterType.LANGUAGE to SessionLanguage.FRENCH.name), viewModel.filterPairs())
    }

    @Test
    fun reapplying_an_equal_content_empty_set_keeps_the_unfiltered_agenda() {
        val unfilteredIds = viewModel.allIds()

        viewModel.onSessionFiltersChanged(emptySet())
        viewModel.onSessionFiltersChanged(emptySet())

        assertEquals(unfilteredIds, viewModel.allIds())
        assertTrue(viewModel.sessionFilters.value.isEmpty())
    }
}
