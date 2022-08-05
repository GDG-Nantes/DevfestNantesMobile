package com.gdgnantes.devfest.android.ui.screens.venue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.model.Venue
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val store: DevFestNantesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState.LOADING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    val venue: StateFlow<Venue>
        get() = store.venue
            .onEach { _uiState.emit(UiState.SUCCESS) }
            .stateIn(viewModelScope, SharingStarted.Lazily, Venue())
}