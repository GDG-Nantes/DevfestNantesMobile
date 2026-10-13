package com.gdgnantes.devfest.feature.agenda.di

import android.content.SharedPreferences
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class AgendaModuleTest {
    // The store, bookmarks, performance monitoring and preferences are bound by :core:data and
    // :core:analytics, not by this module.
    @Test
    fun agenda_module_graph_is_complete_given_upstream_bindings() {
        agendaModule.verify(
            extraTypes =
                listOf(
                    DevFestNantesStore::class,
                    BookmarksStore::class,
                    PerformanceMonitoring::class,
                    SharedPreferences::class,
                ),
        )
    }
}
