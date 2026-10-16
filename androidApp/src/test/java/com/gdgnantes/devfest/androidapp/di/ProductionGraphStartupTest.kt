package com.gdgnantes.devfest.androidapp.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import com.gdgnantes.devfest.shared.di.initKoin
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Starts the unmodified production graph: `initKoin(platformModules = androidModules)` with only
 * `androidContext` and no test doubles. `initKoin` disables overriding, so a duplicate binding across
 * the per-module Koin modules fails here instead of crashing `Application.onCreate` on a device.
 *
 * Firebase runs against a FirebaseApp initialised with dummy options; nothing is faked, so the real
 * Firebase, bookmarks, store, consent and logging definitions are constructed.
 */
@RunWith(RobolectricTestRunner::class)
// A plain Application: the manifest class would start Koin and Firebase Performance on its own.
@Config(sdk = [34], application = Application::class)
class ProductionGraphStartupTest {
    private lateinit var koin: Koin

    @Before
    fun setUp() {
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

        // On a device the androidx.startup provider hands the Apollo SQL cache its Context before any
        // network resolution; Robolectric does not run content providers, so set it the same way.
        // Reflection keeps androidx.startup off the test compile classpath.
        Class.forName("com.apollographql.cache.normalized.sql.ApolloInitializer")
            .getField("context")
            .set(null, application)

        koin = initKoin(platformModules = androidModules) { androidContext(application) }.koin
    }

    @After
    fun tearDown() {
        stopKoin()
        FirebaseApp.clearInstancesForTest()
    }

    @Test
    fun production_graph_starts_without_overrides() {
        val initializers = koin.get<List<ApplicationInitializer>>(named("applicationInitializers"))

        assertEquals(3, initializers.size)
    }

    // The definitions KoinStartupTest replaces with doubles run unchanged here.
    @Test
    fun real_service_definitions_resolve() {
        assertNotNull(koin.get<AnalyticsService>())
        assertNotNull(koin.get<PerformanceMonitoring>())
        assertNotNull(koin.get<BookmarksStore>())
        assertNotNull(koin.get<DevFestNantesStore>())
        assertNotNull(koin.get<DataCollectionSettingsService>())
        assertNotNull(koin.get<Timber.Tree>())
    }
}
