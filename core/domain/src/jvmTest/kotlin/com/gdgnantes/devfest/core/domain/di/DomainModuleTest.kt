package com.gdgnantes.devfest.core.domain.di

import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@OptIn(KoinExperimentalAPI::class)
class DomainModuleTest {
    // The three store types are bound by :core:data and the platform modules, never by the domain.
    private val storeTypes = listOf(DevFestNantesStore::class, BookmarksStore::class, SessionFiltersStore::class)

    @Test
    fun domain_module_graph_is_complete_given_the_store_types() {
        domainModule.verify(extraTypes = storeTypes)
    }

    @Test
    fun domain_module_verify_inspects_the_store_edges() {
        assertFailsWith<MissingKoinDefinitionException> {
            domainModule.verify()
        }
    }

    @Test
    fun classify_session_use_case_resolves_as_a_new_instance_per_get() {
        val koin = koinApplication { modules(domainModule) }.koin

        assertNotSame(koin.get<ClassifySessionUseCase>(), koin.get<ClassifySessionUseCase>())
    }

    @Test
    fun production_domain_dispatcher_binding_is_dispatchers_default() {
        val koin = koinApplication { modules(domainModule) }.koin

        val dispatcher = koin.get<CoroutineDispatcher>(named(DOMAIN_DISPATCHER))

        assertSame(Dispatchers.Default, dispatcher)
        assertSame(dispatcher, koin.get<CoroutineDispatcher>(named(DOMAIN_DISPATCHER)))
    }
}
