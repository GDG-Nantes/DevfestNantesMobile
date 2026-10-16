package com.gdgnantes.devfest.feature.agenda

import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.agenda.utils.SessionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [SessionFiltersService] for tests, backed by a [MutableStateFlow]. It lives in the
 * agenda test sources because the service interface is owned by the agenda feature.
 */
class FakeSessionFiltersService(
    initial: Set<SessionFilter> = emptySet()
) : SessionFiltersService {
    private val state = MutableStateFlow(initial)

    override val filters: StateFlow<Set<SessionFilter>> = state.asStateFlow()

    override fun setFilters(filters: Set<SessionFilter>) {
        state.value = filters
    }
}
