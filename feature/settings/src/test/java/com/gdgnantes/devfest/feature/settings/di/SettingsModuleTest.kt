package com.gdgnantes.devfest.feature.settings.di

import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class SettingsModuleTest {
    // The Firebase handles and SharedPreferences are bound by :core:analytics and :core:data.
    @Test
    fun settings_module_graph_is_complete_given_upstream_bindings() {
        settingsModule.verify(
            extraTypes =
                listOf(
                    FirebaseAnalytics::class,
                    SharedPreferences::class,
                    FirebaseCrashlytics::class,
                    FirebasePerformance::class,
                ),
        )
    }
}
