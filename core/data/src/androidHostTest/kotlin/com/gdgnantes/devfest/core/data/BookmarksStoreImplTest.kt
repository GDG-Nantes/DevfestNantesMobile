package com.gdgnantes.devfest.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Pins how [BookmarksStoreImpl] persists bookmarks (D-17): the production preferences file, the key
 * `selected_sessions` and a string set of session ids. Key literals are written out here on purpose,
 * so renaming the constant or changing the format fails a test and users keep their bookmarks.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BookmarksStoreImplTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().clear().commit()
    }

    @Test
    fun an_empty_store_has_no_bookmarks() = runTest {
        val store = BookmarksStoreImpl(prefs)

        assertEquals(emptySet(), store.bookmarkedSessionIds.first())
        assertFalse(store.isBookmarked("s1"))
    }

    @Test
    fun setBookmarked_stores_the_id_in_a_string_set_under_selected_sessions() {
        val store = BookmarksStoreImpl(prefs)

        store.setBookmarked("s1", true)

        assertEquals(setOf("s1"), prefs.getStringSet("selected_sessions", null))
    }

    @Test
    fun a_store_restores_the_bookmarks_saved_in_the_preferences() = runTest {
        prefs.edit().putStringSet("selected_sessions", setOf("a", "b")).commit()

        val store = BookmarksStoreImpl(prefs)

        assertTrue(store.isBookmarked("a"))
        assertEquals(setOf("a", "b"), store.bookmarkedSessionIds.first())
        assertTrue(store.subscribe("b").first())
        assertFalse(store.subscribe("c").first())
    }

    @Test
    fun removing_a_bookmark_leaves_the_others_in_the_preferences() {
        prefs.edit().putStringSet("selected_sessions", setOf("a", "b")).commit()
        val store = BookmarksStoreImpl(prefs)

        store.setBookmarked("a", false)

        assertEquals(setOf("b"), prefs.getStringSet("selected_sessions", null))
        assertFalse(store.isBookmarked("a"))
    }

    @Test
    fun a_second_store_sees_what_the_first_one_saved() = runTest {
        BookmarksStoreImpl(prefs).setBookmarked("s9", true)

        assertEquals(setOf("s9"), BookmarksStoreImpl(prefs).bookmarkedSessionIds.first())
    }
}
