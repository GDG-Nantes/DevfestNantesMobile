package com.gdgnantes.devfest.feature.agenda.characterization

import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.core.domain.agenda.FilterSessionsUseCase
import com.gdgnantes.devfest.core.domain.agenda.GroupSessionsUseCase
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.FakeSessionFiltersStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
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

    private val filtersService = FakeSessionFiltersStore()

    // Built per test: viewModelScope needs Dispatchers.Main, which the rule installs after construction.
    private val viewModel by lazy {
        val store = FakeDevFestNantesStore()

        AgendaViewModel(
            ObserveAgendaUseCase(
                store,
                filtersService,
                FakeBookmarksStore(),
                FilterSessionsUseCase(),
                GroupSessionsUseCase(),
                mainDispatcherRule.dispatcher,
            ),
            store,
            filtersService,
            NoOpPerformanceMonitoring(),
            ClassifySessionUseCase(),
        )
    }

    private fun AgendaViewModel.allIds(): Set<String> =
        days.value.flatMap { day -> day.slots.flatMap { slot -> slot.sessions.map { it.id } } }.toSet()

    private fun AgendaViewModel.filterPairs(): Set<Pair<SessionFilterType, String>> =
        sessionFilters.value.map { it.type to it.value }.toSet()

    private fun french() = setOf(SessionFilter(SessionFilterType.LANGUAGE, SessionLanguage.FRENCH.name))

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
        assertEquals(setOf(SessionFilterType.LANGUAGE to SessionLanguage.FRENCH.name), viewModel.filterPairs())
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
