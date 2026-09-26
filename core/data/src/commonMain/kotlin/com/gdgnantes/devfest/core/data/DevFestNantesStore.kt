package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.Partner
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.Venue
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

/**
 *
 */
interface DevFestNantesStore {
    @NativeCoroutines
    val agenda: Flow<Agenda>

    @NativeCoroutines
    val partners: Flow<Map<PartnerCategory, List<Partner>>>

    @NativeCoroutines
    suspend fun getRoom(id: String): Room?

    @NativeCoroutines
    val rooms: Flow<Set<Room>>

    @NativeCoroutines
    suspend fun getSession(id: String): Session?

    @NativeCoroutines
    val sessions: Flow<List<Session>>

    @NativeCoroutines
    suspend fun getSpeaker(id: String): Speaker?

    @NativeCoroutines
    suspend fun getSpeakerSessions(speakerId: String): List<Session>

    @NativeCoroutines
    val speakers: Flow<List<Speaker>>

    @NativeCoroutines
    suspend fun getVenue(language: ContentLanguage): Venue
}
