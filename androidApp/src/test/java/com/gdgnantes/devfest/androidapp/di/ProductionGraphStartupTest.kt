package com.gdgnantes.devfest.androidapp.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
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
import kotlin.test.assertEquals

/**
 * Starts the unmodified production graph: `initKoin(platformModules = androidModules)` with only
 * `androidContext` and no test doubles. `initKoin` disables overriding, so a duplicate binding across
 * the per-module Koin modules fails here instead of crashing `Application.onCreate` on a device.
 *
 * Firebase runs against a FirebaseApp initialised with dummy options.
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

        koin = initKoin(platformModules = androidModules) { androidContext(application) }.koin
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun production_graph_starts_without_overrides() {
        val initializers = koin.get<List<ApplicationInitializer>>(named("applicationInitializers"))

        assertEquals(3, initializers.size)
    }
}
