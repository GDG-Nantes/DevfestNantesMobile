package com.gdgnantes.devfest.android.services

import android.content.SharedPreferences
import com.gdgnantes.devfest.store.BookmarksStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarksStoreImpl @Inject constructor(private val sharedPreferences: SharedPreferences) :
    BookmarksStore {

    private val bookmarkedSessions: MutableSet<String> = HashSet()
    private val selectedSessionIds: MutableStateFlow<Set<String>>

    init {
        val prefSet = sharedPreferences.getStringSet(PREF_SELECTED_SESSIONS, null)
        if (prefSet != null) {
            bookmarkedSessions.addAll(prefSet)
        }

        selectedSessionIds = MutableStateFlow(
            mutableSetOf<String>().apply {
                addAll(bookmarkedSessions)
            }
        )
    }

    override fun isBookmarked(id: String): Boolean {
        return bookmarkedSessions.contains(id)
    }

    override fun setBookmarked(sessionId: String, bookmarked: Boolean) {
        if (bookmarked) {
            bookmarkedSessions.add(sessionId)
        } else {
            bookmarkedSessions.remove(sessionId)
        }
        selectedSessionIds.value = mutableSetOf<String>().apply { addAll(bookmarkedSessions) }
        save()
    }

    private fun save() {
        with(sharedPreferences.edit()) {
            putStringSet(PREF_SELECTED_SESSIONS, bookmarkedSessions)
            apply()
        }
    }

    override fun subscribe(id: String): Flow<Boolean> {
        return selectedSessionIds.asStateFlow().map { it.contains(id) }
    }

    companion object {
        const val PREF_SELECTED_SESSIONS = "selected_sessions"
    }
}