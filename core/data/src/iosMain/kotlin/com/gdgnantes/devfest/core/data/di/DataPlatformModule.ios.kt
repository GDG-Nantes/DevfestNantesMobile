package com.gdgnantes.devfest.core.data.di

import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.data.UserDefaultsBookmarksStore
import com.gdgnantes.devfest.core.data.UserDefaultsSessionFiltersStore
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

// New NSUserDefaults keys per D-15 (legacy keys are never touched); the iOS app starts using these
// stores in Phase 9.
internal actual val dataPlatformModule: Module = module {
    single { UserDefaultsBookmarksStore(NSUserDefaults.standardUserDefaults) } bind BookmarksStore::class
    single { UserDefaultsSessionFiltersStore(NSUserDefaults.standardUserDefaults) } bind SessionFiltersStore::class
}
