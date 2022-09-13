package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.store.graphql.GraphQLStore
import com.gdgnantes.devfest.store.graphql.apolloClient

class DevFestNantesStoreBuilder {

    var useMockServer: Boolean = false

    fun build(): DevFestNantesStore {
        return if (useMockServer) {
            DevFestNantesStoreMocked()
        } else {
            GraphQLStore(apolloClient)
        }
    }
}