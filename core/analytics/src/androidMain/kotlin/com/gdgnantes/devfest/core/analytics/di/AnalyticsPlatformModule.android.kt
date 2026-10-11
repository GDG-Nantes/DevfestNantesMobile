package com.gdgnantes.devfest.core.analytics.di

import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.analytics.FirebaseAnalyticsService
import com.gdgnantes.devfest.core.analytics.performance.FirebasePerformanceMonitoring
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// Firebase SDK handles stay factories (as unscoped Hilt providers were); the app-wide services are singles.
internal actual val analyticsPlatformModule: Module =
    module {
        factory { Firebase.analytics }
        factory { FirebaseCrashlytics.getInstance() }
        factory { FirebasePerformance.getInstance() }
        single { FirebaseAnalyticsService(get()) } bind AnalyticsService::class
        single { FirebasePerformanceMonitoring(get()) } bind PerformanceMonitoring::class
    }
