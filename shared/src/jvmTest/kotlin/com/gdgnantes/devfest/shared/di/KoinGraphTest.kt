package com.gdgnantes.devfest.shared.di

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.DevFestNantesStoreBuilder
import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.core.domain.di.domainModule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.stopKoin
import org.koin.core.error.DefinitionOverrideException
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(KoinExperimentalAPI::class)
class KoinGraphTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun shared_modules_graph_is_complete() {
        module { includes(sharedModules) }.verify()
    }

    @Test
    fun shared_modules_include_the_domain_module() {
        assertTrue(domainModule in sharedModules)
    }

    @Test
    fun init_koin_resolves_the_domain_use_cases() {
        assertNotNull(initKoin().koin.get<ClassifySessionUseCase>())
    }

    @Test
    fun init_koin_with_no_platform_modules_resolves_one_shared_store() {
        val koin = initKoin().koin

        val first = koin.get<DevFestNantesStore>()
        val second = koin.get<DevFestNantesStore>()

        assertNotNull(first)
        assertSame(first, second)
    }

    @Test
    fun init_koin_rejects_a_second_definition_of_an_already_bound_type() {
        val duplicate =
            module {
                single<DevFestNantesStore> { DevFestNantesStoreBuilder().setUseMockServer(true).build() }
            }

        assertFailsWith<DefinitionOverrideException> {
            initKoin(platformModules = listOf(duplicate))
        }
    }
}
