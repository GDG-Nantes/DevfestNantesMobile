package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.android.utils.SessionFilter
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.store.BookmarksStore
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val store: DevFestNantesStore,
    private val bookmarksStore: BookmarksStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState.LOADING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    val _days = MutableStateFlow(
        mapOf(
            1 to AgendaDay(1, emptyList()),
            2 to AgendaDay(2, emptyList())
        )
    )

    val days: StateFlow<Map<Int, AgendaDay>>
        get() = _days.asStateFlow()

    val rooms = store.rooms.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    private val _sessionFilters = MutableStateFlow<Set<SessionFilter>>(emptySet())
    val sessionFilters = _sessionFilters.asStateFlow()

    init {
        viewModelScope.launch {
            store.agenda
                .map { agenda -> agenda.days }
                .onEach { _uiState.emit(UiState.SUCCESS) }
                .combine(_sessionFilters) { days, filters ->
                    days to filters
                }
                .collect { (days, filters) ->
                    _days.emit(days.filterSessions(filters))
                }
        }
    }

    fun onRefresh() {
        //TODO
    }

    fun onSessionFiltersChanged(filters: Set<SessionFilter>) {
        viewModelScope.launch {
            _sessionFilters.emit(filters)
        }
    }

    private fun Map<Int, AgendaDay>.filterSessions(
        filterList: Set<SessionFilter>
    ): Map<Int, AgendaDay> {
        val output = mutableMapOf<Int, AgendaDay>()
        forEach { (key, value) ->
            output[key] = AgendaDay(value.dayIndex, value.sessions.filterSessions(filterList))
        }
        return output
    }

    private fun List<Session>.filterSessions(
        filterList: Set<SessionFilter>
    ): List<Session> {
        val filteredSessions = hashSetOf<Session>()
        if (filterList.isEmpty()) {
            filteredSessions.addAll(this)
            return filteredSessions.toList()
        }
        val sessionsByFilterType =
            mutableMapOf<SessionFilter.FilterType, MutableList<Session>>()
        for (filter in filterList) {
            if (!sessionsByFilterType.containsKey(filter.type)) {
                sessionsByFilterType[filter.type] = mutableListOf()
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
                        if (filter.value == session.language) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }
                    SessionFilter.FilterType.ROOM -> {
                        if (filter.value == session.room?.id) {
                            sessionsByFilterType[filter.type]?.add(session)
                        }
                    }
                }
            }
        }
        //get union join of all ScheduleSessions
        val origin = sessionsByFilterType.values.flatten().toMutableList()
        sessionsByFilterType.values.forEach { origin.retainAll(it) }
        return origin
    }
}