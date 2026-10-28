package com.gdgnantes.devfest.shared.di

import com.gdgnantes.devfest.core.analytics.di.analyticsModule
import com.gdgnantes.devfest.core.data.di.dataModule
import com.gdgnantes.devfest.core.domain.di.domainModule
import com.gdgnantes.devfest.core.network.di.networkModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import kotlin.native.HiddenFromObjC

/** Modules of every KMP core module (network, data, analytics, domain), shared by Android and iOS. */
@HiddenFromObjC
val sharedModules: List<Module> = listOf(networkModule, dataModule, analyticsModule, domainModule)

/**
 * Single composition root. Android passes its platform modules and `androidContext`; iOS calls it
 * through `IosDependencies` with no arguments. Overriding is disabled so a duplicate binding across
 * per-module modules fails at start-up instead of silently winning; tests that override re-enable it
 * in [appDeclaration]. No Koin logger is configured on purpose (definitions must not be logged).
 */
@HiddenFromObjC
fun initKoin(
    platformModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication =
    startKoin {
        allowOverride(false)
        appDeclaration()
        modules(sharedModules + platformModules)
    }
