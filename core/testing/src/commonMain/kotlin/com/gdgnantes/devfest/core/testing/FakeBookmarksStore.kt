package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.data.BookmarksStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * In-memory [BookmarksStore] for tests. Starts empty. Like the production store it only promises
 * `Set` semantics, so tests must not rely on iteration order.
 */
class FakeBookmarksStore : BookmarksStore {
    private val bookmarks = MutableStateFlow<Set<String>>(emptySet())

    override val bookmarkedSessionIds: Flow<Set<String>> = bookmarks

    override fun isBookmarked(id: String): Boolean = id in bookmarks.value

    override fun setBookmarked(sessionId: String, bookmarked: Boolean) {
        bookmarks.value = if (bookmarked) bookmarks.value + sessionId else bookmarks.value - sessionId
    }

    override fun subscribe(id: String): Flow<Boolean> = bookmarks.map { id in it }.distinctUntilChanged()
}
