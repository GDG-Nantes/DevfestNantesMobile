package com.gdgnantes.devfest.feature.venue

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.domain.venue.VenueLanguageUseCase
import com.gdgnantes.devfest.core.model.Venue
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class VenueViewModel(
    private val store: DevFestNantesStore,
    private val venueLanguage: VenueLanguageUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState.LOADING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    val venue: StateFlow<Venue?>
        get() =
            flow {
                ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]?.let { locale ->
                    emit(store.getVenue(venueLanguage.languageFor(locale.language)))
                }
            }
                .onEach { _uiState.emit(UiState.SUCCESS) }
                .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
