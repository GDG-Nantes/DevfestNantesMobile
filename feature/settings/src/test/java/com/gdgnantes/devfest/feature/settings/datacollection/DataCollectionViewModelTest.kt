package com.gdgnantes.devfest.feature.settings.datacollection

import com.gdgnantes.devfest.feature.settings.FakeDataCollectionSettingsService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DataCollectionViewModelTest {
    private val service = FakeDataCollectionSettingsService()
    private val viewModel = DataCollectionViewModel(service)

    @Test
    fun single_toggle_is_forwarded() {
        viewModel.onServiceActivationStatusChange(DataCollectionService.FIREBASE_CRASHLYTICS, true)
        viewModel.onServiceActivationStatusChange(DataCollectionService.GOOGLE_ANALYTICS, false)

        assertEquals(
            listOf(
                DataCollectionService.FIREBASE_CRASHLYTICS to true,
                DataCollectionService.GOOGLE_ANALYTICS to false
            ),
            service.activationChanges
        )
        assertEquals(0, service.consentAllCalls)
        assertEquals(0, service.disallowAllCalls)
    }

    @Test
    fun accept_all_calls_consentToAllServices() {
        viewModel.onAllServicesActivationStatusChange(true)

        assertEquals(1, service.consentAllCalls)
        assertEquals(0, service.disallowAllCalls)
    }

    @Test
    fun refuse_all_calls_disallowAllServices() {
        viewModel.onAllServicesActivationStatusChange(false)

        assertEquals(0, service.consentAllCalls)
        assertEquals(1, service.disallowAllCalls)
    }

    @Test
    fun agreement_and_status_are_passed_through() =
        runTest {
            assertFalse(viewModel.isDataCollectionAgreementSet)

            service.agreementSet = true
            assertTrue(viewModel.isDataCollectionAgreementSet)

            val enabled = DataCollectionService.entries.associateWith { it == DataCollectionService.FIREBASE_PERFORMANCE }
            service.status.value = enabled

            assertEquals(enabled, viewModel.dataCollectionServicesActivationStatus.first())
        }
}
