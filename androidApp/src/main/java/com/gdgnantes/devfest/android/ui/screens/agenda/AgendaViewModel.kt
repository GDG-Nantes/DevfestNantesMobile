package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val store: DevFestNantesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState.LOADING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    val days: StateFlow<Map<Int, AgendaDay>>
        get() = store.agenda
            .map { agenda -> agenda.days }
            .onEach { _uiState.emit(UiState.SUCCESS) }
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                mapOf(
                    1 to AgendaDay(1, emptyList()),
                    2 to AgendaDay(2, emptyList())
                )
            )

    fun onRefresh() {
        //TODO
    }
}