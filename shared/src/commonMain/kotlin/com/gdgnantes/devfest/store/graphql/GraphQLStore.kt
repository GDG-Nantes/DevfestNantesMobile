package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.gdgnantes.devfest.graphql.GetSessionQuery
import com.gdgnantes.devfest.model.*
import com.gdgnantes.devfest.store.DevFestNantesStore
import kotlinx.coroutines.flow.Flow

internal class GraphQLStore(private val apolloClient: ApolloClient) : DevFestNantesStore {
    override val agenda: Flow<Agenda>
        get() = TODO("Not yet implemented")
    override val partners: Flow<List<Partner>>
        get() = TODO("Not yet implemented")

    override suspend fun getRoom(id: String): Room? {
        TODO("Not yet implemented")
    }

    override val rooms: Flow<List<Room>>
        get() = TODO("Not yet implemented")

    override suspend fun getSession(id: String): Session? {
        return try {
            val response = apolloClient.query(GetSessionQuery(id)).execute()
            response.dataAssertNoErrors.session.toSession()
        } catch (e: ApolloException) {
            println(e.message)
            null
        }
    }

    override val sessions: Flow<List<Session>>
        get() = TODO("Not yet implemented")

    override suspend fun getSpeaker(id: String): Speaker {
        TODO("Not yet implemented")
    }

    override val speakers: Flow<List<Speaker>>
        get() = TODO("Not yet implemented")
    override val venue: Flow<Venue>
        get() = TODO("Not yet implemented")

}