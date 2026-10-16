package com.gdgnantes.devfest.feature.agenda.services

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.feature.agenda.utils.SessionFilter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins how [SessionFiltersServiceImpl] persists the agenda filters (D-17): the key
 * `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` and one JSON string per filter. Key and JSON are written
 * out as literals so a renamed constant or a changed serializer fails a test and saved filters survive.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionFiltersServiceImplTest {
    private lateinit var prefs: SharedPreferences

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefs = context.getSharedPreferences("test-prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    // SessionFilter is a plain class with identity equality (characterized, not changed).
    private fun Set<SessionFilter>.pairs() = map { it.type to it.value }.toSet()

    @Test
    fun setFilters_stores_each_filter_as_its_json_string() {
        val service = SessionFiltersServiceImpl(prefs)

        service.setFilters(
            setOf(
                SessionFilter(SessionFilter.FilterType.LANGUAGE, "FRENCH"),
                SessionFilter(SessionFilter.FilterType.ROOM, "titan")
            )
        )

        assertEquals(
            setOf(
                """{"type":"LANGUAGE","value":"FRENCH"}""",
                """{"type":"ROOM","value":"titan"}"""
            ),
            prefs.getStringSet("SHARED_PREFERENCES_KEY_AGENDA_FILTERS", null)
        )
    }

    @Test
    fun a_service_restores_the_saved_filters_and_skips_malformed_entries() {
        prefs.edit()
            .putStringSet(
                "SHARED_PREFERENCES_KEY_AGENDA_FILTERS",
                setOf(
                    """{"type":"LANGUAGE","value":"FRENCH"}""",
                    """{"type":"ROOM","value":"titan"}""",
                    "not-json"
                )
            )
            .commit()

        val service = SessionFiltersServiceImpl(prefs)

        assertEquals(
            setOf(
                SessionFilter.FilterType.LANGUAGE to "FRENCH",
                SessionFilter.FilterType.ROOM to "titan"
            ),
            service.filters.value.pairs()
        )
    }

    @Test
    fun setFilters_with_an_empty_set_stores_an_empty_set() {
        val service = SessionFiltersServiceImpl(prefs)
        service.setFilters(setOf(SessionFilter(SessionFilter.FilterType.BOOKMARK, "true")))

        service.setFilters(emptySet())

        assertTrue(prefs.contains("SHARED_PREFERENCES_KEY_AGENDA_FILTERS"))
        assertEquals(emptySet(), prefs.getStringSet("SHARED_PREFERENCES_KEY_AGENDA_FILTERS", null))
        assertTrue(service.filters.value.isEmpty())
    }

    @Test
    fun a_fresh_service_has_no_filters() {
        assertTrue(SessionFiltersServiceImpl(prefs).filters.value.isEmpty())
    }
}
