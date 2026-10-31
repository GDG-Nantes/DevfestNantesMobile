package com.gdgnantes.devfest.core.data

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins how [UserDefaultsSessionFiltersStore] persists filters (D-15): the new key
 * `devfest.agenda.filters`, an array of the shared codec's JSON strings, with the legacy
 * `AgendaSessionFilters` key neither read nor modified. Key literals and JSON are written out on
 * purpose, so changing the format fails a test. Each test uses its own `NSUserDefaults` suite.
 */
class UserDefaultsSessionFiltersStoreTest {
    private val suiteName = "devfest-test-" + NSUUID().UUIDString
    private val defaults = requireNotNull(NSUserDefaults(suiteName = suiteName))
    private val french = SessionFilter(SessionFilterType.LANGUAGE, "FRENCH")
    private val titan = SessionFilter(SessionFilterType.ROOM, "titan")

    @AfterTest
    fun tearDown() {
        defaults.removePersistentDomainForName(suiteName)
    }

    private fun storedStrings(): Set<String>? =
        defaults.arrayForKey("devfest.agenda.filters")?.map { it as String }?.toSet()

    @Test
    fun setFilters_stores_the_android_codec_json_under_the_new_key() {
        val store = UserDefaultsSessionFiltersStore(defaults)

        store.setFilters(setOf(french, titan))

        assertEquals(
            setOf("""{"type":"LANGUAGE","value":"FRENCH"}""", """{"type":"ROOM","value":"titan"}"""),
            storedStrings(),
        )
    }

    @Test
    fun a_second_store_restores_the_saved_filters() {
        UserDefaultsSessionFiltersStore(defaults).setFilters(setOf(french, titan))

        assertEquals(setOf(french, titan), UserDefaultsSessionFiltersStore(defaults).filters.value)
    }

    @Test
    fun malformed_entries_are_skipped_on_load() {
        defaults.setObject(
            listOf("not-json", """{"type":"LANGUAGE","value":"FRENCH"}""", 42),
            forKey = "devfest.agenda.filters",
        )

        assertEquals(setOf(french), UserDefaultsSessionFiltersStore(defaults).filters.value)
    }

    @Test
    fun a_non_array_value_under_the_new_key_gives_an_empty_set() {
        defaults.setObject("not-an-array", forKey = "devfest.agenda.filters")

        assertEquals(emptySet(), UserDefaultsSessionFiltersStore(defaults).filters.value)
    }

    @Test
    fun setting_no_filters_stores_an_empty_array() {
        val store = UserDefaultsSessionFiltersStore(defaults)
        store.setFilters(setOf(french))

        store.setFilters(emptySet())

        assertEquals(emptyList<Any?>(), defaults.arrayForKey("devfest.agenda.filters"))
        assertEquals(emptySet(), UserDefaultsSessionFiltersStore(defaults).filters.value)
    }

    @Test
    fun the_legacy_agenda_session_filters_key_is_never_read_or_modified() {
        val legacyString = """[{"type":"language","value":"french"}]"""
        defaults.setObject(legacyString, forKey = "AgendaSessionFilters")

        val store = UserDefaultsSessionFiltersStore(defaults)
        assertEquals(emptySet(), store.filters.value)
        store.setFilters(setOf(french))

        assertEquals(legacyString, defaults.stringForKey("AgendaSessionFilters"))
    }

    @Test
    fun setting_an_equal_content_set_does_not_re_emit() = runTest {
        val store = UserDefaultsSessionFiltersStore(defaults)
        store.setFilters(setOf(french))

        store.filters.test {
            assertEquals(setOf(french), awaitItem())
            store.setFilters(setOf(french))
            expectNoEvents()
        }
    }

    @Test
    fun concurrent_writers_persist_the_latest_state() = runTest {
        val store = UserDefaultsSessionFiltersStore(defaults)
        val writes = (1..200).map { setOf(SessionFilter(SessionFilterType.ROOM, "room$it")) }

        withContext(Dispatchers.Default) {
            writes.map { filters -> async { store.setFilters(filters) } }.awaitAll()
        }

        val latest = store.filters.value
        assertTrue(latest in writes, "in-memory state must be one of the written sets")
        assertEquals(latest.map(SessionFilterCodec::encode).toSet(), storedStrings())
        assertEquals(latest, UserDefaultsSessionFiltersStore(defaults).filters.value)
    }
}
