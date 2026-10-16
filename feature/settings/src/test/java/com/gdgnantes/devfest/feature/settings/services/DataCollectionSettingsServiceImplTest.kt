package com.gdgnantes.devfest.feature.settings.services

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins the consent persistence of [DataCollectionSettingsServiceImpl] (D-17): the key
 * `SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS`, a string set of enum names, nothing enabled
 * before any choice, and each choice applied to the Firebase collection flags. Key and names are
 * literals so a rename fails a test and users' consent survives.
 *
 * Runs against the real Firebase handles of a FirebaseApp with dummy options. The Crashlytics build
 * id it needs comes from `src/test/res`, which never reaches the packaged library resources.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class DataCollectionSettingsServiceImplTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(application).isEmpty()) {
            FirebaseApp.initializeApp(
                application,
                FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef0123456789")
                    .setApiKey("test-api-key")
                    .setProjectId("test-project")
                    .build()
            )
        }
        prefs = application.getSharedPreferences("test-prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    private fun newService(): DataCollectionSettingsServiceImpl =
        DataCollectionSettingsServiceImpl(
            FirebaseAnalytics.getInstance(ApplicationProvider.getApplicationContext<Application>()),
            prefs,
            FirebaseCrashlytics.getInstance(),
            FirebasePerformance.getInstance()
        )

    @Test
    fun nothing_is_enabled_and_no_agreement_is_set_before_any_choice() = runTest {
        val service = newService()

        assertFalse(service.isDataCollectionAgreementSet)
        assertEquals(
            mapOf(
                DataCollectionService.GOOGLE_ANALYTICS to false,
                DataCollectionService.FIREBASE_CRASHLYTICS to false,
                DataCollectionService.FIREBASE_PERFORMANCE to false
            ),
            service.dataCollectionServicesActivationStatus.first()
        )
    }

    @Test
    fun a_single_toggle_is_stored_by_enum_name_and_sets_the_agreement() = runTest {
        val service = newService()

        service.changeDataServiceActivationStatus(DataCollectionService.GOOGLE_ANALYTICS, true)

        assertEquals(
            setOf("GOOGLE_ANALYTICS"),
            prefs.getStringSet("SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS", null)
        )
        assertTrue(service.isDataCollectionAgreementSet)
        assertEquals(
            true,
            service.dataCollectionServicesActivationStatus.first()[DataCollectionService.GOOGLE_ANALYTICS]
        )
    }

    @Test
    fun consentToAllServices_stores_the_three_names_and_enables_collection() {
        val service = newService()

        service.consentToAllServices()

        assertEquals(
            setOf("GOOGLE_ANALYTICS", "FIREBASE_CRASHLYTICS", "FIREBASE_PERFORMANCE"),
            prefs.getStringSet("SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS", null)
        )
        assertTrue(FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled())
        assertTrue(FirebasePerformance.getInstance().isPerformanceCollectionEnabled)
    }

    @Test
    fun disallowAllServices_stores_an_empty_set_and_disables_collection() {
        val service = newService()
        service.consentToAllServices()

        service.disallowAllServices()

        // The key stays present with an empty set, so "the user already chose" survives.
        assertTrue(prefs.contains("SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS"))
        assertEquals(
            emptySet(),
            prefs.getStringSet("SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS", null)
        )
        assertTrue(service.isDataCollectionAgreementSet)
        assertFalse(FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled())
        assertFalse(FirebasePerformance.getInstance().isPerformanceCollectionEnabled)
    }

    @Test
    fun a_service_restores_the_consent_saved_in_the_preferences() = runTest {
        prefs.edit()
            .putStringSet("SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS", setOf("FIREBASE_PERFORMANCE"))
            .commit()

        val service = newService()

        assertTrue(service.isDataCollectionAgreementSet)
        assertEquals(
            mapOf(
                DataCollectionService.GOOGLE_ANALYTICS to false,
                DataCollectionService.FIREBASE_CRASHLYTICS to false,
                DataCollectionService.FIREBASE_PERFORMANCE to true
            ),
            service.dataCollectionServicesActivationStatus.first()
        )
    }
}
