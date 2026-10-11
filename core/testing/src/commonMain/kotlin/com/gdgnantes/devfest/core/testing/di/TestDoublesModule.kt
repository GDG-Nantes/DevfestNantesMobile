package com.gdgnantes.devfest.core.testing.di

import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.testing.FakeAnalyticsService
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.fakeDevFestNantesStore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Replaces the production store, bookmarks and analytics bindings with in-memory fakes.
 *
 * List it AFTER the production modules and enable overriding, for example
 * `koinApplication { allowOverride(true); modules(sharedModules + testDoublesModule) }`.
 * Without `allowOverride(true)` the duplicate bindings are rejected on purpose. Never reference this
 * module from production code: [org.koin.core.module.Module]s here are test-only.
 */
val testDoublesModule: Module =
    module {
        single<DevFestNantesStore> { fakeDevFestNantesStore() }
        single<BookmarksStore> { FakeBookmarksStore() }
        single<AnalyticsService> { FakeAnalyticsService() }
    }
