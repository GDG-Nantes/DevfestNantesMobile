package com.gdgnantes.devfest.androidapp.ui.screens.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpeakersViewModel @Inject constructor(
    private val store: DevFestNantesStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _speakers = MutableStateFlow<List<Speaker>>(emptyList())
    val speakers = _speakers.asStateFlow()

    init {
        viewModelScope.launch {
            store.speakers.collect {
                _speakers.value = it
                _uiState.value = UiState.SUCCESS
            }
        }
    }
}