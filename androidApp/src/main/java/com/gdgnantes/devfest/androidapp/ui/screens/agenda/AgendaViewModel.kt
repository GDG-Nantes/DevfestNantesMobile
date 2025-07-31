package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceMonitoring
import com.gdgnantes.devfest.androidapp.core.performance.traceDataLoading
import com.gdgnantes.devfest.androidapp.core.performance.traceStateUpdate
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.utils.SessionFilter
import com.gdgnantes.devfest.model.Agenda.Companion.DAY_ONE_ISO
import com.gdgnantes.devfest.model.Agenda.Companion.DAY_TWO_ISO
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.store.BookmarksStore
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val store: DevFestNantesStore,
    private val bookmarksStore: BookmarksStore,
    private val performanceMonitoring: PerformanceMonitoring
) : ViewModel() {
    private var autoRefreshJob: Job? = null

    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _unfilteredDays =
        MutableStateFlow(
            mapOf(
                1 to AgendaDay(1, DAY_ONE_ISO, emptyList()),
                2 to AgendaDay(2, DAY_TWO_ISO, emptyList())
            )
        )
    private val unfilteredDays = _unfilteredDays.asStateFlow()

    private val _days = MutableStateFlow(unfilteredDays.value)
    val days: StateFlow<Map<Int, AgendaDay>>
        get() = _days.asStateFlow()

    val rooms = store.rooms.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    private val _sessionFilters = MutableStateFlow<Set<SessionFilter>>(emptySet())
    val sessionFilters = _sessionFilters.asStateFlow()

    init {
        onRefresh()
    }

    fun onRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob =
            viewModelScope.launch {
                performanceMonitoring.traceDataLoading(
                    operation = PerformanceMonitoring.TRACE_AGENDA_LOAD,
                    dataSource = "graphql"
                ) {
                    store.agenda
                        .map { agenda -> agenda.days }
                        .onEach { days ->
                            _unfilteredDays.value = days
                            Timber.d("Agenda loaded with ${days.values.sumOf { it.sessions.size }} sessions")
                        }
                        .onEach { _uiState.emit(UiState.SUCCESS) }
                        .combine(_sessionFilters) { _, _ -> }
                        .combine(bookmarksStore.bookmarkedSessionIds) { _, _ -> }
                        .collect { updateFilteredDays() }
                }
            }
    }

    fun onSessionFiltersChanged(filters: Set<SessionFilter>) {
        viewModelScope.launch {
            performanceMonitoring.traceStateUpdate("agenda") {
                _sessionFilters.emit(filters)
                Timber.d("Session filters updated: ${filters.size} active filters")
            }
        }
    }

    private suspend fun updateFilteredDays() =
        withContext(Dispatchers.IO) {
            _days.value =
                mutableMapOf<Int, AgendaDay>()
                    .apply {
                        unfilteredDays.value.forEach { (key, value) ->
                            val date = if (key == 1) DAY_ONE_ISO else DAY_TWO_ISO
                            this[key] =
                                AgendaDay(
                                    value.dayIndex, date,
                                    value.sessions
                                        .filterSessions(_sessionFilters.value)
                                        .sortedBy { session -> session.scheduleSlot.startDate }
                                )
                        }
                    }.toMap()
        }

    @Suppress("NestedBlockDepth", "CyclomaticComplexMethod")
    private fun List<Session>.filterSessions(
        filterList: Set<SessionFilter>
    ): List<Session> {
        val filteredSessions = hashSetOf<Session>()
        if (filterList.isEmpty()) {
            filteredSessions.addAll(this)
            return filteredSessions.toList()
        }
        val sessionsByFilterType =
            mutableMapOf<SessionFilter.FilterType, MutableSet<Session>>()
        for (filter in filterList) {
            if (!sessionsByFilterType.containsKey(filter.type)) {
                sessionsByFilterType[filter.type] = mutableSetOf()
            }
        }
        for (session in this) {
            for (filter in filterList) {
                when (filter.type) {
                    SessionFilter.FilterType.BOOKMARK -> {
                        if (bookmarksStore.isBookmarked(session.id)) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }

                    SessionFilter.FilterType.LANGUAGE -> {
                        if (filter.value == session.language?.name) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }

                    SessionFilter.FilterType.COMPLEXITY -> {
                        if (filter.value == session.complexity?.name) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }

                    SessionFilter.FilterType.ROOM -> {
                        if (filter.value == session.room?.id) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }

                    SessionFilter.FilterType.TYPE -> {
                        if (filter.value == session.type?.name) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }
                }
            }
        }
        // get union join of all Sessions
        val origin = sessionsByFilterType.values.flatten().toMutableSet()
        sessionsByFilterType.values.forEach { origin.retainAll(it) }
        return origin.toList()
    }
}
