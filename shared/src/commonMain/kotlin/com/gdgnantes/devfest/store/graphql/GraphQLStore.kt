package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
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
                    if (response.exception != null) {
                        println("Apollo error: ${response.exception}")
                        return@map emptyMap()
                    }
                    response.data?.partnerGroups
                        ?.map { it.toPartnersGroup() }
                        ?.let { groups ->
                            mutableMapOf<PartnerCategory, List<Partner>>().apply {
                                groups.forEach { (category, partners) ->
                                    put(category, partners)
                                }
                            }.toMap()
                        } ?: emptyMap()
                }
                .catch { e ->
                    println(e.message)
                }

    override suspend fun getRoom(id: String): Room? {
        val response = apolloClient.query(GetRoomsQuery()).execute()
        if (response.exception != null) {
            println("Apollo error: ${response.exception}")
            return null
        }
        return response.data?.rooms
            ?.firstOrNull { it.roomDetails.id == id }
            ?.roomDetails?.toRoom()
    }

    override val rooms: Flow<Set<Room>>
        get() =
            apolloClient.query(GetRoomsQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    if (response.exception != null) {
                        println("Apollo error: ${response.exception}")
                        return@map emptySet()
                    }
                    response.data?.rooms
                        ?.map { it.roomDetails.toRoom() }
                        ?.sortedBy { it.sortIndex }
                        ?.toSet() ?: emptySet()
                }
                .catch { e ->
                    println(e.message)
                }

    override suspend fun getSession(id: String): Session? {
        val response = apolloClient.query(GetSessionQuery(id)).execute()
        if (response.exception != null) {
            println("Apollo error: ${response.exception}")
            return null
        }
        return response.data?.session?.toSession()
    }

    override val sessions: Flow<List<Session>>
        get() =
            apolloClient.query(GetSessionsQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    if (response.exception != null) {
                        println("Apollo error: ${response.exception}")
                        return@map emptyList()
                    }
                    response.data?.sessions?.nodes
                        ?.map { it.sessionDetails.toSession() } ?: emptyList()
                }
                .catch { e ->
                    println(e.message)
                }

    override suspend fun getSpeaker(id: String): Speaker? {
        val response = apolloClient.query(GetSpeakersQuery()).execute()
        if (response.exception != null) {
            println("Apollo error: ${response.exception}")
            return null
        }
        return response.data?.speakers
            ?.firstOrNull { it.speakerDetails.id == id }
            ?.speakerDetails?.toSpeaker()
    }

    override suspend fun getSpeakerSessions(speakerId: String): List<Session> {
        val response = apolloClient.query(GetSessionsQuery()).execute()
        if (response.exception != null) {
            println("Apollo error: ${response.exception}")
            return emptyList()
        }
        return response.data?.sessions?.nodes
            ?.map { it.sessionDetails.toSession() }
            ?.filter { it.speakers.any { speaker -> speaker.id == speakerId } } ?: emptyList()
    }

    override val speakers: Flow<List<Speaker>>
        get() =
            apolloClient.query(GetSpeakersQuery())
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .toFlow()
                .map { response ->
                    if (response.exception != null) {
                        println("Apollo error: ${response.exception}")
                        return@map emptyList()
                    }
                    response.data?.speakers
                        ?.map { it.speakerDetails.toSpeaker() } ?: emptyList()
                }
                .catch { e ->
                    println(e.message)
                }

    override suspend fun getVenue(language: ContentLanguage): Venue {
        val response = apolloClient.query(GetVenueQuery(VENUE_ID, language.apiParameter)).execute()
        if (response.exception != null) {
            println("Apollo error: ${response.exception}")
            return buildVenueStub(language) // Fallback
        }
        return response.data?.venue?.toVenue() ?: buildVenueStub(language)
    }

    companion object {
        const val VENUE_ID = "main"
    }
}
