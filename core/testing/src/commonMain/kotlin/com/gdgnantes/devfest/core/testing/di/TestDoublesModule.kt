package com.gdgnantes.devfest.core.testing.di

import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.testing.FakeAnalyticsService
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.FakeSessionFiltersStore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Replaces the production store, bookmarks, session filters and analytics bindings with in-memory fakes. The store is
 * a [FakeDevFestNantesStore], seeded with the shared fixtures. Tests that need to push data or make
 * it fail resolve it from Koin and cast it to that type.
 *
 * List it AFTER the production modules and enable overriding, for example
 * `koinApplication { allowOverride(true); modules(sharedModules + testDoublesModule) }`.
 * Without `allowOverride(true)` the duplicate bindings are rejected on purpose. Never reference this
 * module from production code: [org.koin.core.module.Module]s here are test-only.
 */
val testDoublesModule: Module =
    module {
        single<DevFestNantesStore> { FakeDevFestNantesStore() }
        single<BookmarksStore> { FakeBookmarksStore() }
        single<SessionFiltersStore> { FakeSessionFiltersStore() }
        single<AnalyticsService> { FakeAnalyticsService() }
    }
