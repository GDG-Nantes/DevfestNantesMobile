package com.gdgnantes.devfest.androidapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    sessionFiltersService: SessionFiltersService
) : ViewModel() {
    val isAgendaFiltered: StateFlow<Boolean> =
        sessionFiltersService.filters
            .map { it.isNotEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )
}
