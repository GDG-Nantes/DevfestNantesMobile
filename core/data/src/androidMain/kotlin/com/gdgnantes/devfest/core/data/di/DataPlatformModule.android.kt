package com.gdgnantes.devfest.core.data.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.BookmarksStoreImpl
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.data.SharedPreferencesSessionFiltersStore
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// Same default preferences file as before the DI migration, so bookmarks, filters and consent survive.
// The filters store is a stateful single (agenda and HomeViewModel share one instance): never bind a
// second holder over the filters preference key.
internal actual val dataPlatformModule: Module =
    module {
        single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
        single { BookmarksStoreImpl(get()) } bind BookmarksStore::class
        single { SharedPreferencesSessionFiltersStore(get()) } bind SessionFiltersStore::class
    }
