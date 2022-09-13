package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.store.graphql.GraphQLStore
import com.gdgnantes.devfest.store.graphql.apolloClient

class DevFestNantesStoreBuilder {

    private var useMockServer: Boolean = false

    fun setUseMockServer(useMockServer: Boolean): DevFestNantesStoreBuilder {
        this.useMockServer = useMockServer
        return this
    }

    fun build(): DevFestNantesStore {
        return if (useMockServer) {
            DevFestNantesStoreMocked()
        } else {
            GraphQLStore(apolloClient)
        }
    }
}