package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.Agenda
import com.gdgnantes.devfest.model.ContentLanguage
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.PartnerCategory
import com.gdgnantes.devfest.model.Room
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.Venue
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
    suspend fun getSpeakerSessions(speakerId: String): List<Session>
    val speakers: Flow<List<Speaker>>
    suspend fun getVenue(language: ContentLanguage): Venue
}