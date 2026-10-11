package com.gdgnantes.devfest.core.network.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class NetworkModuleTest {
    @Test
    fun network_module_graph_is_complete() {
        networkModule.verify()
    }
}
