package com.gdgnantes.devfest.core.data.di

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.BookmarksStoreImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// Same default preferences file as before the DI migration, so bookmarks, filters and consent survive.
internal actual val dataPlatformModule: Module =
    module {
        single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
        single { BookmarksStoreImpl(get()) } bind BookmarksStore::class
    }
