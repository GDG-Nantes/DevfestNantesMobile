package com.gdgnantes.devfest.feature.sessiondetail.di

import com.gdgnantes.devfest.feature.sessiondetail.FeedbackFormViewModel
import com.gdgnantes.devfest.feature.sessiondetail.SessionViewModel
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private const val REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL = 300L

val sessionDetailModule: Module =
    module {
        // Explicit positional definition: the session id is bound by position (store, performance
        // monitoring, id), never by type matching.
        viewModel { (sessionId: String) -> SessionViewModel(get(), get(), sessionId) }

        viewModelOf(::FeedbackFormViewModel)

        // A factory, not a single: the settings side effect runs on every FeedbackFormViewModel
        // creation, like the unscoped provider it replaces.
        factory {
            Firebase.remoteConfig.apply {
                setConfigSettingsAsync(
                    remoteConfigSettings {
                        minimumFetchIntervalInSeconds = REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL
                    },
                )
            }
        }
    }
