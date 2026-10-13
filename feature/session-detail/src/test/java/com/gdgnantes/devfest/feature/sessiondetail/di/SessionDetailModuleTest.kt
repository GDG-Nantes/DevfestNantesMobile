package com.gdgnantes.devfest.feature.sessiondetail.di

import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.testing.fakeDevFestNantesStore
import com.gdgnantes.devfest.feature.sessiondetail.OpenFeedbackConfig
import com.gdgnantes.devfest.feature.sessiondetail.SessionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(KoinExperimentalAPI::class, ExperimentalCoroutinesApi::class)
class SessionDetailModuleTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // The store and performance monitoring come from :core:data and :core:analytics, the
    // OpenFeedback configuration from the app-shell appModule.
    @Test
    fun session_detail_module_graph_is_complete_given_upstream_bindings() {
        sessionDetailModule.verify(
            extraTypes =
                listOf(
                    DevFestNantesStore::class,
                    PerformanceMonitoring::class,
                    OpenFeedbackConfig::class,
                ),
        )
    }

    @Test
    fun session_view_model_resolves_with_the_session_id_parameter() {
        val koin =
            koinApplication {
                modules(
                    sessionDetailModule,
                    module {
                        single<DevFestNantesStore> { fakeDevFestNantesStore() }
                        single<PerformanceMonitoring> { NoOpPerformanceMonitoring() }
                    },
                )
            }.koin

        assertNotNull(koin.get<SessionViewModel> { parametersOf("session-1") })
    }
}
