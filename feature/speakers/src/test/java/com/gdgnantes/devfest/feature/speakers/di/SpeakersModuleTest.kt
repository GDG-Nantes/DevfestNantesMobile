package com.gdgnantes.devfest.feature.speakers.di

import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.testing.fakeDevFestNantesStore
import com.gdgnantes.devfest.feature.speakers.SpeakerViewModel
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
class SpeakersModuleTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // The store, performance monitoring and the speaker index use case are bound by :core:data,
    // :core:analytics and :core:domain.
    @Test
    fun speakers_module_graph_is_complete_given_upstream_bindings() {
        speakersModule.verify(
            extraTypes =
                listOf(DevFestNantesStore::class, PerformanceMonitoring::class, SpeakerIndexUseCase::class),
        )
    }

    @Test
    fun speaker_view_model_resolves_with_the_speaker_id_parameter() {
        val koin =
            koinApplication {
                modules(
                    speakersModule,
                    module {
                        single<DevFestNantesStore> { fakeDevFestNantesStore() }
                        single<PerformanceMonitoring> { NoOpPerformanceMonitoring() }
                        single { SpeakerIndexUseCase() }
                    },
                )
            }.koin

        assertNotNull(koin.get<SpeakerViewModel> { parametersOf("speaker-1") })
    }
}
