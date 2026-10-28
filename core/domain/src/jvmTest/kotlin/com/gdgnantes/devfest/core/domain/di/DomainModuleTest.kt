package com.gdgnantes.devfest.core.domain.di

import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertNotSame

@OptIn(KoinExperimentalAPI::class)
class DomainModuleTest {
    @Test
    fun domain_module_graph_is_complete() {
        domainModule.verify()
    }

    @Test
    fun classify_session_use_case_resolves_as_a_new_instance_per_get() {
        val koin = koinApplication { modules(domainModule) }.koin

        assertNotSame(koin.get<ClassifySessionUseCase>(), koin.get<ClassifySessionUseCase>())
    }
}
