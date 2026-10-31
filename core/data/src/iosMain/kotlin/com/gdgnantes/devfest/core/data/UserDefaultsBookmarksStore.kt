package com.gdgnantes.devfest.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import platform.Foundation.NSLock
import platform.Foundation.NSUserDefaults

/**
 * iOS [BookmarksStore] on [NSUserDefaults]: the bookmarked session ids are an array of strings under
 * [KEY_BOOKMARKS].
 *
 * The key is new on purpose (D-15): the v1.0 iOS app stored favorites under a legacy key in another
 * format, which this class never reads, migrates or removes. Phase 9 wires this store into the app.
 *
 * The state is loaded once in the constructor and every write persists the resulting state. Reads
 * return sets, so the persisted array order is not significant. Writes are serialized with a lock, so
 * the persisted array is always the latest in-memory state even with concurrent callers. Persisted
 * values are untrusted: anything that is not an array of strings yields an empty or partial set
 * instead of a crash.
 */
internal class UserDefaultsBookmarksStore(private val defaults: NSUserDefaults) : BookmarksStore {
    private val state = MutableStateFlow(load())
    private val writeLock = NSLock()

    override val bookmarkedSessionIds: Flow<Set<String>> = state

    override fun isBookmarked(id: String): Boolean = id in state.value

    override fun setBookmarked(sessionId: String, bookmarked: Boolean) {
        // Update and persist under one lock: otherwise a writer could persist an older snapshot
        // after a later writer persisted the merged state, losing a change on the next launch.
        writeLock.lock()
        try {
            state.update { if (bookmarked) it + sessionId else it - sessionId }
            defaults.setObject(state.value.toList(), forKey = KEY_BOOKMARKS)
        } finally {
            writeLock.unlock()
        }
    }

    override fun subscribe(id: String): Flow<Boolean> = state.map { id in it }

    private fun load(): Set<String> =
        defaults.arrayForKey(KEY_BOOKMARKS).orEmpty().mapNotNull { it as? String }.toSet()

    internal companion object {
        const val KEY_BOOKMARKS = "devfest.bookmarks.sessionIds"
    }
}
