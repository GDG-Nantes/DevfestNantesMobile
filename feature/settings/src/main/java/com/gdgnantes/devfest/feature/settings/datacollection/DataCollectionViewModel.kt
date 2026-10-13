package com.gdgnantes.devfest.feature.settings.datacollection

import androidx.lifecycle.ViewModel
import com.gdgnantes.devfest.feature.settings.services.DataCollectionService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import kotlinx.coroutines.flow.Flow

class DataCollectionViewModel(
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
