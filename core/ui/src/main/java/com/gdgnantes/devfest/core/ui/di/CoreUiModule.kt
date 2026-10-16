package com.gdgnantes.devfest.core.ui.di

import com.gdgnantes.devfest.core.ui.BookmarksViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifier name of the [CoroutineDispatcher] feature ViewModels run blocking work on. */
const val IO_DISPATCHER: String = "ioDispatcher"

val coreUiModule: Module =
    module {
        viewModelOf(::BookmarksViewModel)

        // Injected so tests can swap it for a test dispatcher. Production binds Dispatchers.IO.
        single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    }
