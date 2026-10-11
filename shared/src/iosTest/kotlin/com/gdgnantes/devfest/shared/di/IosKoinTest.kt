package com.gdgnantes.devfest.shared.di

import com.gdgnantes.devfest.shared.IosDependencies
import org.koin.core.context.stopKoin
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
}
