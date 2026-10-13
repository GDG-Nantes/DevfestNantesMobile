package com.gdgnantes.devfest.feature.speakers.di

import com.gdgnantes.devfest.feature.speakers.SpeakerViewModel
import com.gdgnantes.devfest.feature.speakers.list.SpeakersViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val speakersModule: Module =
    module {
        viewModelOf(::SpeakersViewModel)

        // Explicit positional definition: the speaker id is bound by position (store, performance
        // monitoring, id), never by type matching.
        viewModel { (speakerId: String) -> SpeakerViewModel(get(), get(), speakerId) }
    }
