package com.gdgnantes.devfest.core.data.di

import com.apollographql.apollo.ApolloClient
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.MissingKoinDefinitionException
import org.koin.test.verify.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(KoinExperimentalAPI::class)
class DataModuleTest {
    // ApolloClient is supplied by :core:network's networkModule, not declared in this module.
    @Test
    fun data_module_graph_is_complete_given_the_apollo_client() {
        dataModule.verify(extraTypes = listOf(ApolloClient::class))
    }

    @Test
    fun data_module_verify_inspects_the_graphql_store_to_apollo_client_edge() {
        assertFailsWith<MissingKoinDefinitionException> {
            dataModule.verify()
        }
    }
}
