package com.gdgnantes.devfest.feature.agenda.di

import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersServiceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val agendaModule: Module =
    module {
        viewModelOf(::AgendaViewModel)

        // Stateful filters holder: must stay a single so the agenda screen and the not-yet-migrated
        // HomeViewModel (through its bridge) observe one instance.
        single { SessionFiltersServiceImpl(get()) } bind SessionFiltersService::class
    }
