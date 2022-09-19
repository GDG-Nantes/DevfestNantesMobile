package com.gdgnantes.devfest.androidapp.ui.screens.datasharing

import androidx.lifecycle.ViewModel
import com.gdgnantes.devfest.androidapp.services.DataSharingService
import com.gdgnantes.devfest.androidapp.services.DataSharingSettingsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DataSharingViewModel @Inject constructor(
    private val dataSharingSettingsService: DataSharingSettingsService
) : ViewModel() {

    val dataSharingServicesActivationStatus: Flow<Map<DataSharingService, Boolean>>
        get() = dataSharingSettingsService.dataSharingServicesActivationStatus

    val isDataSharingAgreementSet: Boolean
        get() = dataSharingSettingsService.isDataSharingAgreementSet

    fun onServiceActivationStatusChange(dataSharingService: DataSharingService, enabled: Boolean) {
        dataSharingSettingsService.changeDataServiceActivationStatus(dataSharingService, enabled)
    }

    fun onAllServicesActivationStatusChange(enabled: Boolean) {
        if (enabled) {
            dataSharingSettingsService.consentToAllServices()
        } else {
            dataSharingSettingsService.disallowAllServices()
        }
    }
}
