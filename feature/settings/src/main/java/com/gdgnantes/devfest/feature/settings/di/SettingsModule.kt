package com.gdgnantes.devfest.feature.settings.di

import com.gdgnantes.devfest.feature.settings.datacollection.DataCollectionViewModel
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsServiceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule: Module =
    module {
        viewModelOf(::DataCollectionViewModel)

        // Stateful consent holder: must stay a single so the dialog, the settings screen and the
        // Hilt-built DataSharingInitializer (through its bridge) share one instance.
        single {
            DataCollectionSettingsServiceImpl(get(), get(), get(), get())
        } bind DataCollectionSettingsService::class
    }
