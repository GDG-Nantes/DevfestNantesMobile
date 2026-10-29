package com.gdgnantes.devfest.feature.agenda.di

import com.gdgnantes.devfest.feature.agenda.AgendaViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val agendaModule: Module =
    module {
        // Explicit positional definition: observe agenda use case (:core:domain), store, session
        // filters store, performance monitoring.
        // The session filters store is bound by the :core:data platform module.
        viewModel { AgendaViewModel(get(), get(), get(), get()) }
    }
