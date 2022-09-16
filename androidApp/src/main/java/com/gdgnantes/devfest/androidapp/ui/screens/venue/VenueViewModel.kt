package com.gdgnantes.devfest.androidapp.ui.screens.venue

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.utils.toContentLanguage
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
        get() = flow {
            ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]?.let { locale ->
                emit(store.getVenue(locale.toContentLanguage()))
            }
        }
            .onEach { _uiState.emit(UiState.SUCCESS) }
            .stateIn(viewModelScope, SharingStarted.Lazily, Venue())
}