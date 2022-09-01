package com.gdgnantes.devfest.store

import kotlinx.coroutines.flow.Flow

interface BookmarksStore {
    fun isBookmarked(id: String): Boolean
    fun setBookmarked(sessionId: String, bookmarked: Boolean)
    fun subscribe(id: String): Flow<Boolean>
}