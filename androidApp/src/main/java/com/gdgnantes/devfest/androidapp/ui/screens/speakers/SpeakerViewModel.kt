package com.gdgnantes.devfest.androidapp.ui.screens.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpeakerViewModel @AssistedInject constructor(
    private val store: DevFestNantesStore,
    @Assisted speakerId: String
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
            _speaker.emit(store.getSpeaker(speakerId))
            _uiState.value = UiState.SUCCESS
        }
        viewModelScope.launch {
            _sessions.emit(store.getSpeakerSessions(speakerId))
        }
    }

    @AssistedFactory
    interface SpeakerViewModelFactory {
        fun create(speakerId: String): SpeakerViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: SpeakerViewModelFactory,
            speakerId: String
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(speakerId) as T
                }
            }
    }
}
