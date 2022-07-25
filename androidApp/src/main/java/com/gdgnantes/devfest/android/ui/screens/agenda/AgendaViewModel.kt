package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    val store: DevFestNantesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState.LOADING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    val days: StateFlow<Map<Int, AgendaDay>>
        get() = store.agenda
            .map { agenda -> agenda.days }
            .onEach { _uiState.emit(UiState.SUCCESS) }
            .stateIn(viewModelScope, SharingStarted.Lazily, mapOf())

    fun onRefresh() {
        //TODO add real logic
        viewModelScope.launch {
            _uiState.emit(UiState.LOADING)
            delay(2000)
            _uiState.emit(UiState.SUCCESS)
        }
    }

}