package com.gdgnantes.devfest.androidapp.ui.screens.datacollection

import androidx.lifecycle.ViewModel
import com.gdgnantes.devfest.androidapp.services.DataCollectionService
import com.gdgnantes.devfest.androidapp.services.DataCollectionSettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DataCollectionViewModel @Inject constructor(
    private val dataCollectionSettingsService: DataCollectionSettingsService
) : ViewModel() {
    val dataCollectionServicesActivationStatus: Flow<Map<DataCollectionService, Boolean>>
        get() = dataCollectionSettingsService.dataCollectionServicesActivationStatus

    val isDataCollectionAgreementSet: Boolean
        get() = dataCollectionSettingsService.isDataCollectionAgreementSet

    fun onServiceActivationStatusChange(
        dataCollectionService: DataCollectionService,
        enabled: Boolean
    ) {
        dataCollectionSettingsService.changeDataServiceActivationStatus(
            dataCollectionService,
            enabled
        )
    }

    fun onAllServicesActivationStatusChange(enabled: Boolean) {
        if (enabled) {
            dataCollectionSettingsService.consentToAllServices()
        } else {
            dataCollectionSettingsService.disallowAllServices()
        }
    }
}
