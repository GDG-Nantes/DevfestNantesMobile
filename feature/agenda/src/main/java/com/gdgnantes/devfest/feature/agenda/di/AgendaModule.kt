package com.gdgnantes.devfest.feature.agenda.di

import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.ui.di.IO_DISPATCHER
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersServiceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module

val agendaModule: Module =
    module {
        // Explicit positional definition: store, bookmarks store, performance monitoring, session
        // filters, then the named IO dispatcher (a plain CoroutineDispatcher has no type-unique binding).
        viewModel { AgendaViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }

        // Stateful filters holder: must stay a single so the agenda screen and HomeViewModel observe
        // one instance. One instance bound under two types (bridge until 07-16 replaces it with the
        // :core:data SessionFiltersStore binding): never add a second holder over the preference key.
        single { SessionFiltersServiceImpl(get()) } binds
            arrayOf(SessionFiltersService::class, SessionFiltersStore::class)
    }
