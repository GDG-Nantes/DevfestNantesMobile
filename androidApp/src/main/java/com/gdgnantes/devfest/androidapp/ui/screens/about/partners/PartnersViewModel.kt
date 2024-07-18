package com.gdgnantes.devfest.androidapp.ui.screens.about.partners

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.PartnerCategory
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartnersViewModel @Inject constructor(
    store: DevFestNantesStore,
) : ViewModel() {
    private val _platiniumPartners = MutableStateFlow<List<Partner>>(emptyList())
    val platiniumPartners: StateFlow<List<Partner>> = _platiniumPartners.asStateFlow()

    private val _goldPartners = MutableStateFlow<List<Partner>>(emptyList())
    val goldPartners: StateFlow<List<Partner>> = _goldPartners.asStateFlow()

    private val _virtualPartners = MutableStateFlow<List<Partner>>(emptyList())
    val virtualPartners: StateFlow<List<Partner>> = _virtualPartners.asStateFlow()

    private val _partnersPartners = MutableStateFlow<List<Partner>>(emptyList())
    val partnersPartners: StateFlow<List<Partner>> = _partnersPartners.asStateFlow()

    init {
        viewModelScope.launch {
            store.partners.collectLatest {
                _platiniumPartners.value = it[PartnerCategory.PLATINIUM] ?: emptyList()
                _goldPartners.value = it[PartnerCategory.GOLD] ?: emptyList()
                _virtualPartners.value = it[PartnerCategory.VIRTUAL] ?: emptyList()
                _partnersPartners.value = it[PartnerCategory.PARTNERS] ?: emptyList()
            }
        }
    }
}
