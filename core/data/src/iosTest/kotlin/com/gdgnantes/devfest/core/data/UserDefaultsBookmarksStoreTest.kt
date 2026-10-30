package com.gdgnantes.devfest.core.data

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins how [UserDefaultsBookmarksStore] persists bookmarks (D-15): the new key
 * `devfest.bookmarks.sessionIds` holding an array of session ids, with the legacy `Favorites` key
 * neither read nor modified. Key literals are written out on purpose, so renaming a constant fails
 * a test. Each test uses its own `NSUserDefaults` suite.
 */
class UserDefaultsBookmarksStoreTest {
    private val suiteName = "devfest-test-" + NSUUID().UUIDString
    private val defaults = requireNotNull(NSUserDefaults(suiteName = suiteName))

    @AfterTest
    fun tearDown() {
        defaults.removePersistentDomainForName(suiteName)
    }

    private fun storedIds(key: String = "devfest.bookmarks.sessionIds"): List<*>? = defaults.arrayForKey(key)

    @Test
    fun an_empty_suite_has_no_bookmarks() = runTest {
        val store = UserDefaultsBookmarksStore(defaults)

        assertEquals(emptySet(), store.bookmarkedSessionIds.first())
        assertFalse(store.isBookmarked("s1"))
    }

    @Test
    fun setBookmarked_stores_the_id_in_an_array_under_the_new_key() {
        val store = UserDefaultsBookmarksStore(defaults)

        store.setBookmarked("s1", true)

        assertEquals(listOf("s1"), storedIds())
        assertTrue(store.isBookmarked("s1"))
    }

    @Test
    fun a_second_store_restores_the_saved_ids_and_removing_one_leaves_the_others() = runTest {
        val first = UserDefaultsBookmarksStore(defaults)
        first.setBookmarked("a", true)
        first.setBookmarked("b", true)

        val second = UserDefaultsBookmarksStore(defaults)
        assertEquals(setOf("a", "b"), second.bookmarkedSessionIds.first())
        assertTrue(second.subscribe("b").first())
        assertFalse(second.subscribe("c").first())

        second.setBookmarked("a", false)

        assertEquals(setOf("b"), storedIds()?.map { it as String }?.toSet())
        assertEquals(setOf("b"), UserDefaultsBookmarksStore(defaults).bookmarkedSessionIds.first())
    }

    @Test
    fun the_legacy_favorites_key_is_never_read_or_modified() = runTest {
        defaults.setObject(listOf("a", "b"), forKey = "Favorites")

        val store = UserDefaultsBookmarksStore(defaults)

        assertEquals(emptySet(), store.bookmarkedSessionIds.first())
        store.setBookmarked("c", true)
        assertEquals(listOf("a", "b"), defaults.arrayForKey("Favorites"))
        assertEquals(listOf("c"), storedIds())
    }

    @Test
    fun a_non_array_value_under_the_new_key_gives_an_empty_set() = runTest {
        defaults.setObject("not-an-array", forKey = "devfest.bookmarks.sessionIds")

        val store = UserDefaultsBookmarksStore(defaults)

        assertEquals(emptySet(), store.bookmarkedSessionIds.first())
    }

    @Test
    fun non_string_elements_under_the_new_key_are_skipped() = runTest {
        defaults.setObject(listOf("a", 42, "b"), forKey = "devfest.bookmarks.sessionIds")

        val store = UserDefaultsBookmarksStore(defaults)

        assertEquals(setOf("a", "b"), store.bookmarkedSessionIds.first())
    }

    @Test
    fun bookmarking_an_id_twice_does_not_re_emit() = runTest {
        val store = UserDefaultsBookmarksStore(defaults)

        store.bookmarkedSessionIds.test {
            assertEquals(emptySet(), awaitItem())
            store.setBookmarked("s1", true)
            assertEquals(setOf("s1"), awaitItem())
            store.setBookmarked("s1", true)
            expectNoEvents()
        }
    }

    @Test
    fun concurrent_writers_leave_every_bookmark_persisted() = runTest {
        val store = UserDefaultsBookmarksStore(defaults)
        val ids = (1..200).map { "s$it" }

        withContext(Dispatchers.Default) {
            ids.map { id -> async { store.setBookmarked(id, true) } }.awaitAll()
        }

        assertEquals(ids.toSet(), store.bookmarkedSessionIds.first())
        assertEquals(ids.toSet(), storedIds()?.map { it as String }?.toSet())
    }
}
