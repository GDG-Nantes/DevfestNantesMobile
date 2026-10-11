package com.gdgnantes.devfest.core.ui.di

import com.gdgnantes.devfest.core.ui.BookmarksViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val coreUiModule: Module =
    module {
        viewModelOf(::BookmarksViewModel)
    }
