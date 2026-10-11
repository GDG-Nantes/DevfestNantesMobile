package com.gdgnantes.devfest.androidapp.di

import android.app.Activity
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.DataSharingInitializer
import com.gdgnantes.devfest.androidapp.core.OpenFeedbackInitializer
import com.gdgnantes.devfest.androidapp.core.logging.TimberTreeDebug
import com.gdgnantes.devfest.androidapp.core.logging.TimberTreeRelease
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceInitializer
import com.gdgnantes.devfest.androidapp.services.ExternalContentService
import com.gdgnantes.devfest.androidapp.ui.screens.home.HomeViewModel
import com.gdgnantes.devfest.feature.sessiondetail.OpenFeedbackConfig
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber

/** App-shell Koin bindings: dispatchers, logging, application initializers and the home ViewModel. */
val appModule: Module =
    module {
        single {
            CoroutinesDispatcherProvider(
                default = Dispatchers.Default,
                computation = Dispatchers.Default,
                io = Dispatchers.IO,
                main = Dispatchers.Main.immediate
            )
        }

        single<Timber.Tree> {
            if (BuildConfig.DEBUG) TimberTreeDebug(get()) else TimberTreeRelease(get())
        }

        factory { DataSharingInitializer(get()) }
        factory { OpenFeedbackInitializer(androidApplication()) }
        factory { PerformanceInitializer() }

        // The order is the consent contract: stored data-collection consent must be applied before
        // performance collection is forced on. An explicit list, never an unordered multibinding.
        single(named("applicationInitializers")) {
            listOf<ApplicationInitializer>(
                get<DataSharingInitializer>(),
                get<OpenFeedbackInitializer>(),
                get<PerformanceInitializer>()
            )
        }

        // One instance per MainActivity, like the previous activity scope; Koin keeps no reference.
        factory { (activity: Activity) -> ExternalContentService(activity) }

        viewModelOf(::HomeViewModel)

        // Only the flag and the project id cross into :feature:session-detail.
        factory {
            OpenFeedbackConfig(
                enabled = BuildConfig.OPEN_FEEDBACK_ENABLED.toBoolean(),
                projectId = BuildConfig.OPEN_FEEDBACK_PROJECT_ID
            )
        }
    }
