package com.gdgnantes.devfest.feature.agenda.di

import android.content.SharedPreferences
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.ui.di.IO_DISPATCHER
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

@OptIn(KoinExperimentalAPI::class)
class AgendaModuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The store, bookmarks, performance monitoring, preferences and the IO dispatcher are bound by
    // :core:data, :core:analytics and :core:ui, not by this module.
    @Test
    fun agenda_module_graph_is_complete_given_upstream_bindings() {
        agendaModule.verify(
            extraTypes =
                listOf(
                    DevFestNantesStore::class,
                    BookmarksStore::class,
                    PerformanceMonitoring::class,
                    SharedPreferences::class,
                    CoroutineDispatcher::class,
                ),
        )
    }

    // The agenda is the only consumer of this binding, so this is where it is pinned.
    @Test
    fun production_io_dispatcher_binding_is_dispatchers_io() {
        val koin = koinApplication { modules(coreUiModule) }.koin

        assertSame(Dispatchers.IO, koin.get<CoroutineDispatcher>(named(IO_DISPATCHER)))
    }

    @Test
    fun agenda_view_model_resolves_with_the_named_io_dispatcher() {
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
                    module {
                        single<DevFestNantesStore> { FakeDevFestNantesStore() }
                        single<BookmarksStore> { FakeBookmarksStore() }
                        single<PerformanceMonitoring> { NoOpPerformanceMonitoring() }
                        single<SessionFiltersStore> { filters }
                        single<CoroutineDispatcher>(named(IO_DISPATCHER)) { mainDispatcherRule.dispatcher }
                    }
                )
            }.koin

        assertNotNull(koin.get<AgendaViewModel>())
    }
}
