package com.gdgnantes.devfest.core.domain.di

import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Koin bindings of `:core:domain`. Use cases are `factory` definitions (they hold no state, so a new
 * instance per `get()` is fine). Hidden from Swift like `dataModule`: iOS resolves use cases through
 * `IosDependencies`, never through the module value.
 */
@HiddenFromObjC
val domainModule: Module =
    module {
        factory { ClassifySessionUseCase() }
    }
