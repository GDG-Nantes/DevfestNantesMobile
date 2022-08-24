package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.*
import kotlinx.coroutines.flow.Flow

/**
 *
 */
interface DevFestNantesStore {
    val agenda: Flow<Agenda>
    suspend fun getBookmarks(userId: String): Set<String>
    suspend fun setBookmark(userId: String, sessionId: String, value: Boolean)
    val partners: Flow<List<Partner>>
    suspend fun getRoom(id: String): Room?
    val rooms: Flow<List<Room>>
    suspend fun getSession(id: String): Session
    val sessions: Flow<List<Session>>
    suspend fun getSpeaker(id: String): Speaker
    val speakers: Flow<List<Speaker>>
    val venue: Flow<Venue>
}