package com.gdgnantes.devfest.feature.about.di

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class AboutModuleTest {
    // The store is bound by :core:data's dataModule, not by this module.
    @Test
    fun about_module_graph_is_complete_given_the_store() {
        aboutModule.verify(extraTypes = listOf(DevFestNantesStore::class))
    }
}
