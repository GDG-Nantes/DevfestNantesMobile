package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.store.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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

    fun getAgenda(): Flow<Result<Agenda>> {
        val sessionsFlow = getSessions()
        val roomsFlow = getRooms()
        val speakersFlow = getSpeakers()

        return combine(
            sessionsFlow,
            roomsFlow,
            speakersFlow
        ) { sessions, rooms, speakers ->
            val exception = sessions.exceptionOrNull()
                ?: rooms.exceptionOrNull()
                ?: speakers.exceptionOrNull()

            if (exception != null) {
                Result.failure(exception)
            } else {
                Result.success(
                    Agenda(
                        sessions.getOrThrow().associateBy { it.id },
                        rooms.getOrThrow().associateBy { it.id },
                        speakers.getOrThrow().associateBy { it.id }
                    )
                )
            }
        }
    }
}