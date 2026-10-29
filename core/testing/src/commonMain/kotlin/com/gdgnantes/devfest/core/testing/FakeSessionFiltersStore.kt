package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.model.SessionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [SessionFiltersStore] for tests. Like the production stores it is backed by a
 * `StateFlow`, so setting an equal-content set does not re-emit.
 */
class FakeSessionFiltersStore(
    initial: Set<SessionFilter> = emptySet(),
) : SessionFiltersStore {
    private val _filters = MutableStateFlow(initial)

    override val filters: StateFlow<Set<SessionFilter>> = _filters.asStateFlow()

    override fun setFilters(filters: Set<SessionFilter>) {
        _filters.value = filters
    }
}
