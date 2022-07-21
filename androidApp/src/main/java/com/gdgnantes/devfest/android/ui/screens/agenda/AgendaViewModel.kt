package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    val store: DevFestNantesStore
) : ViewModel() {

    val days: StateFlow<Map<Int, AgendaDay>>
        get() = store.agenda.map { agenda -> agenda.days }
            .stateIn(viewModelScope, SharingStarted.Lazily, mapOf())
}