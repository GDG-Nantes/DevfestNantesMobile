package com.gdgnantes.devfest.feature.settings

import com.gdgnantes.devfest.feature.settings.services.DataCollectionService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Recording [DataCollectionSettingsService] for tests. It lives in the settings test sources
 * because the service interface is owned by the settings feature.
 *
 * Calls are recorded in [activationChanges], [consentAllCalls] and [disallowAllCalls]; the
 * status map and [agreementSet] are plain mutable state the test can set.
 */
class FakeDataCollectionSettingsService(
    initialStatus: Map<DataCollectionService, Boolean> =
        DataCollectionService.entries.associateWith { false },
    var agreementSet: Boolean = false
) : DataCollectionSettingsService {
    val status = MutableStateFlow(initialStatus)

    val activationChanges = mutableListOf<Pair<DataCollectionService, Boolean>>()
    var consentAllCalls = 0
        private set
    var disallowAllCalls = 0
        private set

    override val isDataCollectionAgreementSet: Boolean
        get() = agreementSet

    override val dataCollectionServicesActivationStatus = status

    override fun changeDataServiceActivationStatus(
        dataCollectionService: DataCollectionService,
        enabled: Boolean
    ) {
        activationChanges += dataCollectionService to enabled
    }

    override fun consentToAllServices() {
        consentAllCalls++
    }

    override fun disallowAllServices() {
        disallowAllCalls++
    }

    override fun updatesDataServicesActivationStatus() = Unit
}
