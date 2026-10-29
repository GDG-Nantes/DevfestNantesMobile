package com.gdgnantes.devfest.feature.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.analytics.performance.traceDataLoading
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.core.domain.agenda.DaySchedule
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.core.domain.agenda.UtcDayWindows
import com.gdgnantes.devfest.core.model.Agenda.Companion.DAY_ONE_ISO
import com.gdgnantes.devfest.core.model.Agenda.Companion.DAY_TWO_ISO
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import timber.log.Timber

class AgendaViewModel(
    private val observeAgenda: ObserveAgendaUseCase,
    store: DevFestNantesStore,
    private val sessionFiltersStore: SessionFiltersStore,
    private val performanceMonitoring: PerformanceMonitoring,
    private val classifySession: ClassifySessionUseCase
) : ViewModel() {
    private var autoRefreshJob: Job? = null

    private val dayAssignment = UtcDayWindows(listOf(DAY_ONE_ISO, DAY_TWO_ISO))

    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _days =
        MutableStateFlow(
            listOf(
                DaySchedule(1, DAY_ONE_ISO, emptyList()),
                DaySchedule(2, DAY_TWO_ISO, emptyList())
            )
        )
    val days: StateFlow<List<DaySchedule>>
        get() = _days.asStateFlow()

    val rooms = store.rooms.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val sessionFilters = sessionFiltersStore.filters

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
                    // The zone is read at each refresh and passed explicitly (D-05, D-19); UtcDayWindows
                    // ignores it by design.
                    observeAgenda.observe(dayAssignment, TimeZone.currentSystemDefault()).collect { days ->
                        _days.value = days
                        Timber.d(
                            "Agenda loaded with ${days.sumOf { day -> day.slots.sumOf { it.sessions.size } }} sessions"
                        )
                        _uiState.emit(UiState.SUCCESS)
                    }
                }
            }
    }

    /** True when the row of [session] shows the bookmark star (a typed session that is not a service slot). */
    fun isTalk(session: Session): Boolean = classifySession.isTalk(session)

    fun onSessionFiltersChanged(filters: Set<SessionFilter>) {
        viewModelScope.launch {
            sessionFiltersStore.setFilters(filters)
            Timber.d("Session filters updated: ${filters.size} active filters")
        }
    }
}
