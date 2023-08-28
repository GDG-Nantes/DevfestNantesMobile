package com.gdgnantes.devfest.androidapp.ui.screens.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    private val _speaker = MutableStateFlow<Speaker?>(null)
    val speaker: StateFlow<Speaker?>
        get() = _speaker

    init {
        viewModelScope.launch {
            _speaker.emit(store.getSpeaker(speakerId))
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
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(speakerId) as T
            }
        }
    }
}