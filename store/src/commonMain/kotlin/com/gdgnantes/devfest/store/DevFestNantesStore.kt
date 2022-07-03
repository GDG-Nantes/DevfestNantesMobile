package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.store.model.*
import kotlinx.coroutines.flow.Flow

/**
 *
 */
interface DevFestNantesStore {
    fun getVenue(id: String): Flow<Result<Venue>>
    fun getSpeaker(id: String): Flow<Result<Speaker>>
    fun getRoom(id: String): Flow<Result<Room>>
    fun getSession(id: String): Flow<Result<Session>>
    fun getPartners(): Flow<Result<List<Partner>>>
    fun getSessions(): Flow<Result<List<Session>>>
    fun getSpeakers(): Flow<Result<List<Speaker>>>
    fun getRooms(): Flow<Result<List<Room>>>
    fun getBookmarks(userId: String): Flow<Result<Set<String>>>
    suspend fun setBookmark(userId: String, sessionId: String, value: Boolean)

    // Kotlin/Native client

}