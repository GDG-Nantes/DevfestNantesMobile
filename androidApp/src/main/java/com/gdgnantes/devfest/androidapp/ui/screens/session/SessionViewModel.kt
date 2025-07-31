package com.gdgnantes.devfest.androidapp.ui.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceMonitoring
import com.gdgnantes.devfest.androidapp.core.performance.traceDataLoading
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SessionViewModel @AssistedInject constructor(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring,
    @Assisted sessionId: String
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

    @AssistedFactory
    interface SessionViewModelFactory {
        fun create(sessionId: String): SessionViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: SessionViewModelFactory,
            sessionId: String
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(sessionId) as T
            }
        }
    }
}
