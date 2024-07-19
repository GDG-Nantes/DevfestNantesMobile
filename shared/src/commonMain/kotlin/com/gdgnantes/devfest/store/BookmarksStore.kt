package com.gdgnantes.devfest.store

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

interface BookmarksStore {
    @NativeCoroutines
    val bookmarkedSessionIds: Flow<Set<String>>
    fun isBookmarked(id: String): Boolean
    fun setBookmarked(sessionId: String, bookmarked: Boolean)

    @NativeCoroutines
    fun subscribe(id: String): Flow<Boolean>
}
