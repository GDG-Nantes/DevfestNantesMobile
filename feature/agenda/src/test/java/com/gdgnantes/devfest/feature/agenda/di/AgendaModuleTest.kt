package com.gdgnantes.devfest.feature.agenda.di

import android.content.SharedPreferences
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.core.domain.di.domainModule
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(KoinExperimentalAPI::class)
class AgendaModuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The store, performance monitoring, preferences and the observe agenda use case are bound by
    // :core:data, :core:analytics, the platform and :core:domain, not by this module.
    @Test
    fun agenda_module_graph_is_complete_given_upstream_bindings() {
        agendaModule.verify(
            extraTypes =
                listOf(
                    DevFestNantesStore::class,
                    PerformanceMonitoring::class,
                    SharedPreferences::class,
                    ObserveAgendaUseCase::class,
                ),
        )
    }

    @Test
    fun agenda_view_model_resolves_on_the_domain_module() {
        val filters =
            object : SessionFiltersStore {
                override val filters: StateFlow<Set<SessionFilter>> = MutableStateFlow(emptySet())

                override fun setFilters(filters: Set<SessionFilter>) = Unit
            }
        val koin =
            koinApplication {
                allowOverride(true)
                modules(
                    agendaModule,
                    domainModule,
                    module {
                        single<DevFestNantesStore> { FakeDevFestNantesStore() }
                        single<BookmarksStore> { FakeBookmarksStore() }
                        single<PerformanceMonitoring> { NoOpPerformanceMonitoring() }
                        single<SessionFiltersStore> { filters }
                    }
                )
            }.koin

        assertNotNull(koin.get<AgendaViewModel>())
    }
}
