package com.gdgnantes.devfest.androidapp.di

import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.feature.sessiondetail.OpenFeedbackConfig
import org.koin.core.module.Module
import org.koin.dsl.module

/** App-shell Koin bindings that need the app's `BuildConfig`. */
val appModule: Module =
    module {
        // Only the flag and the project id cross into :feature:session-detail.
        factory {
            OpenFeedbackConfig(
                enabled = BuildConfig.OPEN_FEEDBACK_ENABLED.toBoolean(),
                projectId = BuildConfig.OPEN_FEEDBACK_PROJECT_ID
            )
        }
    }
