package com.gdgnantes.devfest.feature.settings

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.settings.di.settingsModule
import com.gdgnantes.devfest.feature.settings.services.DataCollectionService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import com.gdgnantes.devfest.core.ui.R as CoreUiR

/**
 * Records consent calls. The production implementation needs the Firebase handles and shared
 * preferences, which the library test application does not provide.
 */
private class RecordingDataCollectionSettingsService : DataCollectionSettingsService {
    private val status =
        MutableStateFlow(DataCollectionService.values().associateWith { false })

    var consentAllCalls = 0
        private set

    override val isDataCollectionAgreementSet: Boolean = false
    override val dataCollectionServicesActivationStatus: Flow<Map<DataCollectionService, Boolean>> =
        status.asStateFlow()

    override fun changeDataServiceActivationStatus(
        dataCollectionService: DataCollectionService,
        enabled: Boolean
    ) {
        status.value = status.value + (dataCollectionService to enabled)
    }

    override fun consentToAllServices() {
        consentAllCalls++
        status.value = DataCollectionService.values().associateWith { true }
    }

    override fun disallowAllServices() {
        status.value = DataCollectionService.values().associateWith { false }
    }

    override fun updatesDataServicesActivationStatus() = Unit
}

@RunWith(AndroidJUnit4::class)
class SettingsSmokeTest {

    private val fake = RecordingDataCollectionSettingsService()

    @get:Rule(order = 0)
    val koinRule =
        KoinTestRule.create {
            allowOverride(true)
            modules(settingsModule, coreUiModule)
            modules(androidTestDoubles)
            modules(module { single<DataCollectionSettingsService> { fake } })
        }

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun accept_all_records_consent_and_turns_every_switch_on() {
        composeRule.setContent { DataCollectionSettingsRoute(onBackClick = {}) }

        listOf(
            R.string.legal_data_collection_google_analytics_title,
            R.string.legal_data_collection_firebase_crashlytics_title,
            R.string.legal_data_collection_firebase_performance_title,
        ).forEach { titleRes ->
            composeRule.onNodeWithText(targetContext.getString(titleRes)).performScrollTo().assertIsDisplayed()
        }
        // The "accept all" switch plus one per service.
        composeRule.onAllNodes(isToggleable()).assertCountEquals(DataCollectionService.values().size + 1)

        composeRule.onNodeWithText(targetContext.getString(R.string.legal_data_collection_accept_all)).assertIsDisplayed()
        // The "accept all" switch is the first toggleable node of the screen.
        composeRule.onAllNodes(isToggleable())[0].performScrollTo().performClick()

        composeRule.waitForIdle()
        assertEquals(1, fake.consentAllCalls)
        composeRule.onAllNodes(isToggleable()).assertAll(isOn())
    }

    @Test
    fun settings_data_sharing_item_reports_navigation() {
        var dataSharingClicks = 0
        composeRule.setContent {
            SettingsRoute(
                versionName = "test",
                versionCode = 1,
                onBackClick = {},
                onLegalClick = {},
                onOpenDataSharing = { dataSharingClicks++ },
                onSupportClick = {},
            )
        }

        composeRule.onNodeWithText(targetContext.getString(CoreUiR.string.screen_data_collection)).performClick()

        assertEquals(1, dataSharingClicks)
    }
}
