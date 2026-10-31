package com.gdgnantes.devfest.androidapp.ui.screens.home

import app.cash.turbine.test
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The agenda test sources are not visible from :androidApp, so the fake is local.
    private class FakeFilters : SessionFiltersStore {
        val state = MutableStateFlow<Set<SessionFilter>>(emptySet())
        override val filters: StateFlow<Set<SessionFilter>> = state

        override fun setFilters(filters: Set<SessionFilter>) {
            state.value = filters
        }
    }

    private val filters = FakeFilters()
    private val anyFilter = SessionFilter(SessionFilterType.BOOKMARK, "")

    private fun viewModel() = HomeViewModel(filters)

    @Test
    fun is_not_filtered_without_filters() =
        runTest {
            viewModel().isAgendaFiltered.test {
                assertEquals(false, awaitItem())
                expectNoEvents()
            }
        }

    @Test
    fun becomes_filtered_when_any_filter_is_set_and_back_when_cleared() =
        runTest {
            viewModel().isAgendaFiltered.test {
                assertEquals(false, awaitItem())

                filters.setFilters(setOf(anyFilter))
                assertEquals(true, awaitItem())

                filters.setFilters(emptySet())
                assertEquals(false, awaitItem())
            }
        }

    @Test
    fun value_stays_false_without_a_subscriber() {
        filters.setFilters(setOf(anyFilter))

        // WhileSubscribed(5000) only starts upstream collection once someone subscribes.
        assertEquals(false, viewModel().isAgendaFiltered.value)
    }
}
