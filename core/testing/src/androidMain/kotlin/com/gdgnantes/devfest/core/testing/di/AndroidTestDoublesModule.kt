package com.gdgnantes.devfest.core.testing.di

import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android-only test doubles; see [testDoublesModule] for the override rules. */
val androidTestDoublesModule: Module =
    module {
        single<PerformanceMonitoring> { NoOpPerformanceMonitoring() }
    }

/** Every test double for an Android graph, ready to append after the production modules. */
val androidTestDoubles: List<Module> = listOf(testDoublesModule, androidTestDoublesModule)
