package com.gdgnantes.devfest.androidapp.di

import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.about.di.aboutModule
import com.gdgnantes.devfest.feature.agenda.di.agendaModule
import com.gdgnantes.devfest.feature.sessiondetail.di.sessionDetailModule
import com.gdgnantes.devfest.feature.settings.di.settingsModule
import com.gdgnantes.devfest.feature.speakers.di.speakersModule
import com.gdgnantes.devfest.feature.venue.di.venueModule
import org.koin.core.module.Module

/** Exact list of Android-side Koin modules passed to `initKoin`; later migration steps append here. */
val androidModules: List<Module> =
    listOf(
        coreUiModule,
        venueModule,
        aboutModule,
        settingsModule,
        speakersModule,
        agendaModule,
        sessionDetailModule,
        appModule,
    )
