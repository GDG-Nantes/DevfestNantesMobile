package com.gdgnantes.devfest.feature.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SpeakerViewModel(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring,
    private val speakerId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _speaker = MutableStateFlow<Speaker?>(null)
    val speaker: StateFlow<Speaker?>
        get() = _speaker

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>>
        get() = _sessions

    init {
        viewModelScope.launch {
            performanceMonitoring.trace(
                traceName = PerformanceMonitoring.TRACE_SPEAKER_DETAILS_LOAD,
                attributes =
                    mapOf(
                        PerformanceMonitoring.ATTR_DATA_SOURCE to "graphql",
                        "speaker_id" to speakerId
                    )
            ) {
                val speakerDetails = store.getSpeaker(speakerId)
                _speaker.emit(speakerDetails)
                _uiState.value = UiState.SUCCESS

                Timber.d("Speaker details loaded: ${speakerDetails?.name ?: "Unknown"}")
                speakerDetails
            }
        }
        viewModelScope.launch {
            val speakerSessions = store.getSpeakerSessions(speakerId)
            _sessions.emit(speakerSessions)

            Timber.d("Speaker sessions loaded: ${speakerSessions.size} sessions")
        }
    }
}
