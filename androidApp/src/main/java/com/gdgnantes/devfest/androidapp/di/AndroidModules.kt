package com.gdgnantes.devfest.androidapp.di

import com.gdgnantes.devfest.feature.about.di.aboutModule
import com.gdgnantes.devfest.feature.venue.di.venueModule
import org.koin.core.module.Module

/** Exact list of Android-side Koin modules passed to `initKoin`; later migration steps append here. */
val androidModules: List<Module> = listOf(venueModule, aboutModule)
