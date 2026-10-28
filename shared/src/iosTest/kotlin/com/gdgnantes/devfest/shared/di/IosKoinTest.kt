package com.gdgnantes.devfest.shared.di

import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.shared.IosDependencies
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class IosKoinTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun start_is_idempotent_and_store_is_one_shared_instance() {
        IosDependencies.start()
        IosDependencies.start()

        val first = IosDependencies.store()
        val second = IosDependencies.store()

        assertNotNull(first)
        assertSame(first, second)
    }

    @Test
    fun domain_use_cases_resolve_after_start() {
        IosDependencies.start()

        assertNotNull(KoinPlatform.getKoin().get<ClassifySessionUseCase>())
    }
}
