package com.gdgnantes.devfest.androidapp.di

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.DataSharingInitializer
import com.gdgnantes.devfest.androidapp.core.OpenFeedbackInitializer
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceInitializer
import com.gdgnantes.devfest.androidapp.services.ExternalContentService
import com.gdgnantes.devfest.androidapp.ui.screens.home.HomeViewModel
import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.BookmarksViewModel
import com.gdgnantes.devfest.feature.about.partners.PartnersViewModel
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.sessiondetail.FeedbackFlags
import com.gdgnantes.devfest.feature.sessiondetail.FeedbackFormViewModel
import com.gdgnantes.devfest.feature.sessiondetail.FirebaseFeedbackFlags
import com.gdgnantes.devfest.feature.sessiondetail.SessionViewModel
import com.gdgnantes.devfest.feature.settings.datacollection.DataCollectionViewModel
import com.gdgnantes.devfest.feature.settings.services.DataCollectionService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import com.gdgnantes.devfest.feature.speakers.SpeakerViewModel
import com.gdgnantes.devfest.feature.speakers.list.SpeakersViewModel
import com.gdgnantes.devfest.feature.venue.VenueViewModel
import com.gdgnantes.devfest.shared.di.initKoin
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

/**
 * D-12 layer 2: starts Koin through the production `initKoin` path under a real (Robolectric)
 * Application and resolves every ViewModel and entry-point dependency, so a missing binding or a
 * wrong parameter fails here instead of on a device.
 *
 * Overriding is re-enabled and doubles are layered on top, so this test does NOT exercise the
 * production definitions the doubles replace. The `:core:testing` doubles replace
 * `DevFestNantesStore`, `BookmarksStore`, `AnalyticsService` and `PerformanceMonitoring`; test-local
 * fakes replace `DataCollectionSettingsService` and `Timber.Tree`. Only `FirebaseRemoteConfig` (against
 * a FirebaseApp initialised with dummy options) and `SessionFiltersServiceImpl` run unfaked here;
 * `FeedbackFormViewModel` is resolved against an offline `FeedbackFlags` so no fetch starts. The
 * unmodified production graph, including those real definitions, is covered by
 * [ProductionGraphStartupTest].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
// A plain Application: the manifest class would start Koin and Firebase Performance on its own, before
// this test controls the FirebaseApp and the overrides.
@Config(sdk = [34], application = Application::class)
class KoinStartupTest {
    private lateinit var koin: Koin

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()

        val application = ApplicationProvider.getApplicationContext<Application>()
        // FirebaseApp is a process-wide static that Robolectric can carry from one test to the next;
        // start from a clean slate and reset again in tearDown.
        FirebaseApp.clearInstancesForTest()
        FirebaseApp.initializeApp(
            application,
            FirebaseOptions.Builder()
                .setApplicationId("1:1234567890:android:abcdef0123456789")
                .setApiKey("test-api-key")
                .setProjectId("test-project")
                .build()
        )

        koin =
            initKoin(platformModules = androidModules + androidTestDoubles + testOverrides) {
                allowOverride(true)
                androidContext(application)
            }.koin
    }

    @After
    fun tearDown() {
        stopKoin()
        FirebaseApp.clearInstancesForTest()
        Dispatchers.resetMain()
    }

    @Test
    fun every_view_model_resolves() {
        assertNotNull(koin.get<VenueViewModel>())
        assertNotNull(koin.get<PartnersViewModel>())
        assertNotNull(koin.get<DataCollectionViewModel>())
        assertNotNull(koin.get<SpeakersViewModel>())
        assertNotNull(koin.get<AgendaViewModel>())
        assertNotNull(koin.get<BookmarksViewModel>())
        assertNotNull(koin.get<HomeViewModel>())
    }

    @Test
    fun parameterized_view_models_resolve() {
        assertNotNull(koin.get<SpeakerViewModel> { parametersOf("speaker-1") })
        assertNotNull(koin.get<SessionViewModel> { parametersOf("session-1") })
    }

    // The production FeedbackFlags binding wraps the real remote config. Resolving it builds the
    // FirebaseRemoteConfig handle against the dummy FirebaseApp but does not fetch: the fetch only
    // starts in FeedbackFormViewModel's init block, which this test deliberately does not run.
    @Test
    fun feedback_flags_resolve_to_the_real_remote_config_binding() {
        assertIs<FirebaseFeedbackFlags>(koin.get<FeedbackFlags>())
    }

    // FeedbackFormViewModel fetches in its init block (D-23), so it is resolved against a no-op
    // FeedbackFlags: the CI unit-test job stays offline and deterministic.
    @Test
    fun feedback_form_view_model_resolves_with_offline_flags() {
        koin.loadModules(
            listOf(module { factory<FeedbackFlags> { OfflineFeedbackFlags } })
        )

        assertNotNull(koin.get<FeedbackFormViewModel>())
    }

    @Test
    fun entry_point_dependencies_resolve() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()

        assertNotNull(koin.get<AnalyticsService>())
        assertNotNull(koin.get<CoroutinesDispatcherProvider>())
        assertNotNull(koin.get<Timber.Tree>())
        assertNotNull(koin.get<ExternalContentService> { parametersOf(activity) })
    }

    // DI-02 / T-04-19: stored consent is applied before performance collection is forced on.
    @Test
    fun application_initializers_run_in_consent_first_order() {
        val initializers = koin.get<List<ApplicationInitializer>>(named("applicationInitializers"))

        assertEquals(
            listOf(
                DataSharingInitializer::class,
                OpenFeedbackInitializer::class,
                PerformanceInitializer::class
            ),
            initializers.map { it::class }
        )
    }

    private val testOverrides =
        listOf(
            module {
                single<DataCollectionSettingsService> { FakeDataCollectionSettingsService() }
                single<Timber.Tree> { Timber.DebugTree() }
            }
        )

    private object OfflineFeedbackFlags : FeedbackFlags {
        override fun getBoolean(key: String): Boolean = false

        override fun fetchAndActivate(onComplete: (successful: Boolean, activated: Boolean) -> Unit) =
            Unit
    }

    private class FakeDataCollectionSettingsService : DataCollectionSettingsService {
        override val isDataCollectionAgreementSet: Boolean = false
        override val dataCollectionServicesActivationStatus: Flow<Map<DataCollectionService, Boolean>> =
            MutableStateFlow(DataCollectionService.values().associateWith { false })

        override fun changeDataServiceActivationStatus(
            dataCollectionService: DataCollectionService,
            enabled: Boolean
        ) = Unit

        override fun consentToAllServices() = Unit

        override fun disallowAllServices() = Unit

        override fun updatesDataServicesActivationStatus() = Unit
    }
}
