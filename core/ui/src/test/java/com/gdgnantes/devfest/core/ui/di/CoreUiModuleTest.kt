package com.gdgnantes.devfest.core.ui.di

import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.testing.di.testDoublesModule
import com.gdgnantes.devfest.core.ui.BookmarksViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.test.verify.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@OptIn(KoinExperimentalAPI::class, ExperimentalCoroutinesApi::class)
class CoreUiModuleTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // The analytics service and the bookmarks store are bound by :core:analytics and :core:data.
    @Test
    fun core_ui_module_graph_is_complete_given_upstream_bindings() {
        coreUiModule.verify(
            extraTypes = listOf(AnalyticsService::class, BookmarksStore::class),
        )
    }

    // The shared fakes of :core:testing are usable from another module (DI-05).
    @Test
    fun bookmarks_view_model_resolves_on_the_shared_test_doubles() {
        val koin = koinApplication { modules(coreUiModule, testDoublesModule) }.koin

        assertNotNull(koin.get<BookmarksViewModel>())
    }
}
