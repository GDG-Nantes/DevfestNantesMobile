package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    store: DevFestNantesStore,
    private val bookmarksStore: BookmarksStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val unfilteredDays = store.agenda
        .map { agenda -> agenda.days }
        .onEach { _uiState.emit(UiState.SUCCESS) }
        .stateIn(
            viewModelScope, SharingStarted.Lazily, mapOf(
                1 to AgendaDay(1, DAY_ONE_ISO, emptyList()),
                2 to AgendaDay(2, DAY_TWO_ISO, emptyList())
            )
        )

    private val _days = MutableStateFlow(unfilteredDays.value)
    val days: StateFlow<Map<Int, AgendaDay>>
        get() = _days.asStateFlow()

    val rooms = store.rooms.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    private val _sessionFilters = MutableStateFlow<Set<SessionFilter>>(emptySet())
    val sessionFilters = _sessionFilters.asStateFlow()

    init {
        viewModelScope.launch {
            unfilteredDays
                .combine(_sessionFilters) { _, _ -> }
                .combine(bookmarksStore.bookmarkedSessionIds) { _, _ -> }
                .collect { updateFilteredDays() }
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

    private suspend fun updateFilteredDays() = withContext(Dispatchers.IO) {
        _days.value = mutableMapOf<Int, AgendaDay>().apply {
            unfilteredDays.value.forEach { (key, value) ->
                val date = if (key == 1) DAY_ONE_ISO else DAY_TWO_ISO
                this[key] =
                    AgendaDay(value.dayIndex, date, value.sessions
                        .filterSessions(_sessionFilters.value)
                        .sortedBy { session -> session.scheduleSlot.startDate })
            }
        }.toMap()
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
                        if (filter.value == session.language?.name) {
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