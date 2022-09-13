package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.gdgnantes.devfest.graphql.*
import com.gdgnantes.devfest.model.*
import com.gdgnantes.devfest.store.DevFestNantesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class GraphQLStore(private val apolloClient: ApolloClient) : DevFestNantesStore {
    override val agenda: Flow<Agenda>
        get() = TODO("Not yet implemented")

    override val partners: Flow<Map<PartnerCategory, List<Partner>>>
        get() = flow {
            try {
                val response = apolloClient.query(GetPartnerGroupsQuery()).execute()
                response.dataAssertNoErrors.partnerGroups
                    .map { it.toPartnersGroup() }
                    .let { groups ->
                        mutableMapOf<PartnerCategory, List<Partner>>().apply {
                            groups.forEach { (category, partners) ->
                                put(category, partners)
                            }
                        }
                            .toMap()
                            .run { emit(this) }
                    }
            } catch (e: ApolloException) {
                println(e.message)
            }
        }

    override suspend fun getRoom(id: String): Room? {
        return try {
            val response = apolloClient.query(GetRoomsQuery()).execute()
            response.dataAssertNoErrors.rooms
                .firstOrNull { it.roomDetails.id == id }
                ?.roomDetails?.toRoom()
        } catch (e: ApolloException) {
            println(e.message)
            null
        }
    }

    override val rooms: Flow<List<Room>>
        get() = flow {
            try {
                val response = apolloClient.query(GetRoomsQuery()).execute()
                response.dataAssertNoErrors.rooms
                    .map { it.roomDetails.toRoom() }
                    .let { emit(it) }
            } catch (e: ApolloException) {
                println(e.message)
            }
        }

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
        get() = flow {
            try {
                val response = apolloClient.query(GetSessionsQuery()).execute()
                response.dataAssertNoErrors.sessions.nodes
                    .map { it.sessionDetails.toSession() }
                    .let { emit(it) }
            } catch (e: ApolloException) {
                println(e.message)
            }
        }

    override suspend fun getSpeaker(id: String): Speaker? {
        return try {
            val response = apolloClient.query(GetSpeakersQuery()).execute()
            response.dataAssertNoErrors.speakers
                .firstOrNull { it.speakerDetails.id == id }
                ?.speakerDetails?.toSpeaker()
        } catch (e: ApolloException) {
            println(e.message)
            null
        }
    }

    override val speakers: Flow<List<Speaker>>
        get() = flow {
            try {
                val response = apolloClient.query(GetSpeakersQuery()).execute()
                response.dataAssertNoErrors.speakers
                    .map { it.speakerDetails.toSpeaker() }
                    .let { emit(it) }
            } catch (e: ApolloException) {
                println(e.message)
            }
        }

    override val venue: Flow<Venue>
        get() = flow {
            try {
                val response = apolloClient.query(GetVenueQuery("1")).execute()
                response.dataAssertNoErrors.venue.venueDetails.toVenue()
                    .let { emit(it) }
            } catch (e: ApolloException) {
                println(e.message)
            }
        }

}