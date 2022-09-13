package com.gdgnantes.devfest.android.ui.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel @AssistedInject constructor(
    private val store: DevFestNantesStore,
    @Assisted sessionId: String
) : ViewModel() {

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?>
        get() = _session

    init {
        viewModelScope.launch {
            _session.emit(store.getSession(sessionId))
        }
    }

    @AssistedFactory
    interface SessionViewModelFactory {
        fun create(sessionId: String): SessionViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: SessionViewModelFactory,
            sessionId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(sessionId) as T
            }
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface AssistedInjectModule