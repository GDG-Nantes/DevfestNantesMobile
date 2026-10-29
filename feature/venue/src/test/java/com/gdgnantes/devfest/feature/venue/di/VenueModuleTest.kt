package com.gdgnantes.devfest.feature.venue.di

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.domain.venue.VenueLanguageUseCase
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class VenueModuleTest {
    // The store and the venue language use case are bound by :core:data's dataModule and :core:domain,
    // not by this module.
    @Test
    fun venue_module_graph_is_complete_given_upstream_bindings() {
        venueModule.verify(extraTypes = listOf(DevFestNantesStore::class, VenueLanguageUseCase::class))
    }
}
