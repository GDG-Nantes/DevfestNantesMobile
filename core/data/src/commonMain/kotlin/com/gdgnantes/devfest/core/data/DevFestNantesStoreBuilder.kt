package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.data.graphql.GraphQLStore
import com.gdgnantes.devfest.core.network.apolloClient

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
