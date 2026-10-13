package com.gdgnantes.devfest.core.network.di

import com.apollographql.apollo.ApolloClient
import com.gdgnantes.devfest.core.network.apolloClient
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Koin bindings of `:core:network`: the process-global [apolloClient] exposed as a single so
 * data-layer definitions can receive it through the graph instead of reaching for the global.
 */
@HiddenFromObjC
val networkModule: Module =
    module {
        single<ApolloClient> { apolloClient }
    }
