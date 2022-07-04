package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.store.model.*
import kotlinx.coroutines.flow.Flow

/**
 *
 */
interface DevFestNantesStore {
    fun getBookmarks(userId: String): Flow<Set<String>>
    suspend fun setBookmark(userId: String, sessionId: String, value: Boolean)
    val partners: Flow<List<Partner>>
    fun getRoom(id: String): Flow<Room>
    fun getRooms(): Flow<List<Room>>
    fun getSession(id: String): Flow<Session>
    fun getSessions(): Flow<List<Session>>
    fun getSpeaker(id: String): Flow<Speaker>
    fun getSpeakers(): Flow<List<Speaker>>
    fun getVenue(id: String): Flow<Venue>


}