package com.gdgnantes.devfest.feature.speakers.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.analytics.performance.traceDataLoading
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SpeakersViewModel(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring,
    private val speakerIndex: SpeakerIndexUseCase
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

    /**
     * The A-Z scroller map: lowercase first letter to the index of the first speaker with that
     * letter, in list order. The shared index yields an empty letter for a speaker with an empty
     * name, which v1.0 crashed on (`NoSuchElementException`); it is skipped here and reported as a
     * non-fatal exception (D-20). Only speaker ids (public conference data) are reported.
     */
    fun firstLetterIndexes(speakers: List<Speaker>): Map<Char, Int> {
        val indexes = speakerIndex.firstIndexes(speakers)
        if (indexes.any { it.letter.isEmpty() }) {
            val ids = speakers.filter { it.name.isEmpty() }.joinToString { it.id }
            Timber.w(
                IllegalStateException("Speaker A-Z index: speaker with an empty name skipped (ids: $ids)"),
                "Speaker A-Z index: empty speaker name skipped"
            )
        }
        return indexes
            .filter { it.letter.isNotEmpty() }
            .associate { it.letter.lowercase().first() to it.index }
    }
}
