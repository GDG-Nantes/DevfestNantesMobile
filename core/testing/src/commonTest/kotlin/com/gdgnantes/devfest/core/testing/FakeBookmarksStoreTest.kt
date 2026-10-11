package com.gdgnantes.devfest.core.testing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeBookmarksStoreTest {
    @Test
    fun a_fresh_store_is_empty() =
        runTest {
            val store = FakeBookmarksStore()

            assertEquals(emptySet(), store.bookmarkedSessionIds.first())
            assertFalse(store.isBookmarked("a"))
            assertFalse(store.subscribe("a").first())
        }

    @Test
    fun bookmarking_a_session_is_visible_everywhere() =
        runTest {
            val store = FakeBookmarksStore()

            store.setBookmarked("a", true)

            assertTrue(store.subscribe("a").first())
            assertTrue(store.isBookmarked("a"))
            assertTrue("a" in store.bookmarkedSessionIds.first())
        }

    @Test
    fun removing_the_bookmark_empties_the_set_again() =
        runTest {
            val store = FakeBookmarksStore()
            store.setBookmarked("a", true)

            store.setBookmarked("a", false)

            assertEquals(emptySet(), store.bookmarkedSessionIds.first())
            assertFalse(store.subscribe("a").first())
        }
}
