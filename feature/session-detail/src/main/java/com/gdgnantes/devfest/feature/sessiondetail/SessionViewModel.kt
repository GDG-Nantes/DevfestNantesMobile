package com.gdgnantes.devfest.feature.sessiondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.analytics.performance.traceDataLoading
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SessionViewModel(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring,
    sessionId: String
) : ViewModel() {
    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?>
        get() = _session

    init {
        viewModelScope.launch {
            performanceMonitoring.traceDataLoading(
                operation = PerformanceMonitoring.TRACE_SESSION_DETAILS_LOAD,
                dataSource = "graphql"
            ) {
                val sessionData = store.getSession(sessionId)
                _session.emit(sessionData)

                Timber.d("Session loaded: ${sessionData?.title ?: "Session not found"}")
                sessionData
            }
        }
    }
}
