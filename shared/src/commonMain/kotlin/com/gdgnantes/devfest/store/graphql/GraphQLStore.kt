package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.exception.ApolloException
import com.gdgnantes.devfest.graphql.GetPartnerGroupsQuery
import com.gdgnantes.devfest.graphql.GetRoomsQuery
import com.gdgnantes.devfest.graphql.GetSessionQuery
import com.gdgnantes.devfest.graphql.GetSessionsQuery
import com.gdgnantes.devfest.graphql.GetSpeakersQuery
import com.gdgnantes.devfest.graphql.GetVenueQuery
import com.gdgnantes.devfest.model.Agenda
import com.gdgnantes.devfest.model.ContentLanguage
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.PartnerCategory
import com.gdgnantes.devfest.model.Room
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.Venue
import com.gdgnantes.devfest.model.stubs.buildVenueStub
import com.gdgnantes.devfest.store.DevFestNantesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class GraphQLStore(private val apolloClient: ApolloClient) : DevFestNantesStore {
    override val agenda: Flow<Agenda>
        get() =
            sessions.map { sessions ->
                Agenda.Builder().run {
                    this.sessions = sessions
                    build()
                }
            }

    override val partners: Flow<Map<PartnerCategory, List<Partner>>>
        get() =
            apolloClient.query(GetPartnerGroupsQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    response.dataAssertNoErrors.partnerGroups
                        .map { it.toPartnersGroup() }
                        .let { groups ->
                            mutableMapOf<PartnerCategory, List<Partner>>().apply {
                                groups.forEach { (category, partners) ->
                                    put(category, partners)
                                }
                            }.toMap()
                        }
                }
                .catch { e ->
                    println(e.message)
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

    override val rooms: Flow<Set<Room>>
        get() =
            apolloClient.query(GetRoomsQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    response.dataAssertNoErrors.rooms
                        .map { it.roomDetails.toRoom() }
                        .sortedBy { it.sortIndex }
                        .toSet()
                }
                .catch { e ->
                    println(e.message)
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
        get() =
            apolloClient.query(GetSessionsQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    response.dataAssertNoErrors.sessions.nodes
                        .map { it.sessionDetails.toSession() }
                }
                .catch { e ->
                    println(e.message)
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

    override suspend fun getSpeakerSessions(speakerId: String): List<Session> {
        return try {
            val response = apolloClient.query(GetSessionsQuery()).execute()
            response.dataAssertNoErrors.sessions.nodes
                .map { it.sessionDetails.toSession() }
                .filter { it.speakers.any { speaker -> speaker.id == speakerId } }
        } catch (e: ApolloException) {
            println(e.message)
            emptyList()
        }
    }

    override val speakers: Flow<List<Speaker>>
        get() =
            apolloClient.query(GetSpeakersQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    response.dataAssertNoErrors.speakers
                        .map { it.speakerDetails.toSpeaker() }
                }
                .catch { e ->
                    println(e.message)
                }

    override suspend fun getVenue(language: ContentLanguage): Venue {
        return try {
            val response =
                apolloClient.query(GetVenueQuery(VENUE_ID, language.apiParameter)).execute()
            response.dataAssertNoErrors.venue.toVenue()
        } catch (e: ApolloException) {
            println(e.message)
            buildVenueStub(language) // Fallback
        }
    }

    companion object {
        const val VENUE_ID = "main"
    }
}
