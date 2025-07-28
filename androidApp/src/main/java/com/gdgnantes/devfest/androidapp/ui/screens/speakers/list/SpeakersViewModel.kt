package com.gdgnantes.devfest.androidapp.ui.screens.speakers.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceMonitoring
import com.gdgnantes.devfest.androidapp.core.performance.traceDataLoading
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SpeakersViewModel @Inject constructor(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _speakers = MutableStateFlow<List<Speaker>>(emptyList())
    val speakers = _speakers.asStateFlow()

    init {
        viewModelScope.launch {
            performanceMonitoring.traceDataLoading(
                operation = PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
                dataSource = "graphql"
            ) {
                store.speakers.collect { speakersList ->
                    _speakers.value = speakersList
                    _uiState.value = UiState.SUCCESS

                    Timber.d("Speakers loaded: ${speakersList.size} speakers")
                    speakersList
                }
            }
        }
    }
}
