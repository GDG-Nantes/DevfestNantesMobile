package com.gdgnantes.devfest.core.testing.di

import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.testing.FakeAnalyticsService
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.FakeSessionFiltersStore
import org.koin.core.error.DefinitionOverrideException
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class TestDoublesModuleTest {
    // Stands in for a production binding; any BookmarksStore that is not the fake will do.
    private val productionLikeBookmarks = module { single<BookmarksStore> { object : BookmarksStore by FakeBookmarksStore() {} } }

    @Test
    fun test_doubles_win_when_listed_after_production_with_overriding_enabled() {
        val koin =
            koinApplication {
                allowOverride(true)
                modules(productionLikeBookmarks, testDoublesModule)
            }.koin

        assertIs<FakeBookmarksStore>(koin.get<BookmarksStore>())
        assertIs<FakeAnalyticsService>(koin.get<AnalyticsService>())
        assertIs<FakeDevFestNantesStore>(koin.get<DevFestNantesStore>())
        assertIs<FakeSessionFiltersStore>(koin.get<SessionFiltersStore>())
    }

    @Test
    fun overrides_must_be_explicit() {
        assertFailsWith<DefinitionOverrideException> {
            koinApplication {
                allowOverride(false)
                modules(productionLikeBookmarks, testDoublesModule)
            }
        }
    }
}
