package com.gdgnantes.devfest.feature.venue.di

import com.gdgnantes.devfest.feature.venue.VenueViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val venueModule: Module =
    module {
        viewModelOf(::VenueViewModel)
    }
