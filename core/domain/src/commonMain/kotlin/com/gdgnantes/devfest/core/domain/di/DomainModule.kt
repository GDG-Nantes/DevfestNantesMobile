package com.gdgnantes.devfest.core.domain.di

import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.core.domain.agenda.FilterSessionsUseCase
import com.gdgnantes.devfest.core.domain.agenda.GroupSessionsUseCase
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.domain.venue.VenueLanguageUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Qualifier of the dispatcher the combined agenda flow runs its upstream work on (D-03). It lives in
 * the domain module, shared by both platforms; Koin forbids a second binding of the same type and
 * qualifier.
 */
internal const val DOMAIN_DISPATCHER = "domainDispatcher"

/**
 * Koin bindings of `:core:domain`. Use cases are `factory` definitions (they hold no state, so a new
 * instance per `get()` is fine). Hidden from Swift like `dataModule`: iOS resolves use cases through
 * `IosDependencies`, never through the module value.
 */
@HiddenFromObjC
val domainModule: Module =
    module {
        single<CoroutineDispatcher>(named(DOMAIN_DISPATCHER)) { Dispatchers.Default }

        factory { ClassifySessionUseCase() }
        factory { FilterSessionsUseCase() }
        factory { GroupSessionsUseCase() }
        factory { SpeakerIndexUseCase() }
        factory { VenueLanguageUseCase() }

        // Positional arguments: store (DevFestNantesStore), sessionFiltersStore (SessionFiltersStore),
        // bookmarksStore (BookmarksStore), filterSessions, groupSessions, dispatcher (domainDispatcher).
        factory { ObserveAgendaUseCase(get(), get(), get(), get(), get(), get(named(DOMAIN_DISPATCHER))) }
    }
