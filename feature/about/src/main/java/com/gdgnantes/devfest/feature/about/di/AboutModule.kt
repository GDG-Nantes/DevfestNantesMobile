package com.gdgnantes.devfest.feature.about.di

import com.gdgnantes.devfest.feature.about.partners.PartnersViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule: Module =
    module {
        viewModelOf(::PartnersViewModel)
    }
