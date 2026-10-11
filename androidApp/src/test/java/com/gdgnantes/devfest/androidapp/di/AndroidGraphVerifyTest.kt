package com.gdgnantes.devfest.androidapp.di

import android.app.Activity
import android.app.Application
import android.content.Context
import com.gdgnantes.devfest.core.analytics.FirebaseAnalyticsService
import com.gdgnantes.devfest.core.analytics.di.analyticsModule
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import com.gdgnantes.devfest.shared.di.sharedModules
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(KoinExperimentalAPI::class)
class AndroidGraphVerifyTest {
    // Context and Application are supplied at runtime by androidContext(), not by a module.
    private val androidRuntimeTypes = listOf(Context::class, Application::class)

    // ExternalContentService receives its Activity as a Koin parameter at the call site, and
    // CoroutinesDispatcherProvider is built from the Dispatchers constants: both parameter types are
    // supplied by the caller, never by a definition.
    private val appShellFrameworkTypes = listOf(Activity::class, CoroutineDispatcher::class)

    // OpenFeedbackConfig is a data class built from BuildConfig constants in appModule; its primitive
    // constructor parameters are values, not injectable dependencies.
    private val configValueTypes = listOf(Boolean::class, String::class)

    // Firebase SDK handles are built by the SDK itself (Firebase.analytics, getInstance()), never by
    // Koin, so their SDK-internal constructor parameters have no definition. They are derived from the
    // SDK classes instead of being named, because the internal types are obfuscated and change per release.
    private val firebaseSdkTypes =
        listOf(FirebaseAnalytics::class, FirebaseCrashlytics::class, FirebasePerformance::class)
            .flatMap { sdkType -> sdkType.java.declaredConstructors.flatMap { it.parameterTypes.toList() } }
            .map { it.kotlin }
            .distinct()

    @Test
    fun full_android_graph_is_complete() {
        module { includes(sharedModules + androidModules) }
            .verify(
                extraTypes = androidRuntimeTypes + firebaseSdkTypes + configValueTypes + appShellFrameworkTypes
            )
    }

    // appModule consumes bindings that other modules declare; they are listed as upstream types so the
    // app-shell definitions are checked on their own (FA-04-06).
    @Test
    fun app_module_is_complete_given_its_upstream_types() {
        appModule.verify(
            extraTypes =
                listOf(
                    DataCollectionSettingsService::class,
                    FirebaseCrashlytics::class,
                    SessionFiltersService::class
                ) + androidRuntimeTypes + appShellFrameworkTypes + configValueTypes
        )
    }

    @Test
    fun android_modules_alone_are_incomplete_so_verify_really_bites() {
        assertFailsWith<MissingKoinDefinitionException> {
            module { includes(androidModules) }.verify(extraTypes = androidRuntimeTypes)
        }
    }

    // Isolation check of the :core:analytics android actual (FA-04-02): it is self-contained.
    @Test
    fun analytics_module_is_complete_on_its_own() {
        analyticsModule.verify(extraTypes = firebaseSdkTypes)
    }

    @Test
    fun analytics_service_alone_is_incomplete_so_verify_really_bites() {
        assertFailsWith<MissingKoinDefinitionException> {
            module { single { FirebaseAnalyticsService(get()) } }.verify(extraTypes = firebaseSdkTypes)
        }
    }
}
