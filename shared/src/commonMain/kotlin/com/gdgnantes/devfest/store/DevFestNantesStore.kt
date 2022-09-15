package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.*
import kotlinx.coroutines.flow.Flow

/**
 *
 */
interface DevFestNantesStore {
    val agenda: Flow<Agenda>
    val partners: Flow<Map<PartnerCategory, List<Partner>>>
    suspend fun getRoom(id: String): Room?
    val rooms: Flow<Set<Room>>
    suspend fun getSession(id: String): Session?
    val sessions: Flow<List<Session>>
    suspend fun getSpeaker(id: String): Speaker?
    val speakers: Flow<List<Speaker>>
    suspend fun getVenue(language: ContentLanguage): Venue
}