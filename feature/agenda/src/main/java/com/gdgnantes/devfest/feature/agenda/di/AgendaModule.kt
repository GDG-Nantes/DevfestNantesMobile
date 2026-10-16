package com.gdgnantes.devfest.feature.agenda.di

import com.gdgnantes.devfest.core.ui.di.IO_DISPATCHER
import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersServiceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val agendaModule: Module =
    module {
        // Explicit positional definition: store, bookmarks store, performance monitoring, session
        // filters, then the named IO dispatcher (a plain CoroutineDispatcher has no type-unique binding).
        viewModel { AgendaViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }

        // Stateful filters holder: must stay a single so the agenda screen and the not-yet-migrated
        // HomeViewModel (through its bridge) observe one instance.
        single { SessionFiltersServiceImpl(get()) } bind SessionFiltersService::class
    }
