package com.gdgnantes.devfest.core.testing

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeSessionFiltersStoreTest {
    private val french = SessionFilter(SessionFilterType.LANGUAGE, "FRENCH")
    private val titan = SessionFilter(SessionFilterType.ROOM, "titan")

    @Test
    fun a_fresh_store_is_empty() {
        assertEquals(emptySet(), FakeSessionFiltersStore().filters.value)
    }

    @Test
    fun the_store_starts_with_its_initial_set() {
        assertEquals(setOf(french), FakeSessionFiltersStore(setOf(french)).filters.value)
    }

    @Test
    fun set_filters_updates_the_current_value() {
        val store = FakeSessionFiltersStore()

        store.setFilters(setOf(french, titan))

        assertEquals(setOf(french, titan), store.filters.value)
    }

    @Test
    fun setting_an_equal_content_set_does_not_re_emit() =
        runTest {
            val store = FakeSessionFiltersStore(setOf(french))

            store.filters.test {
                assertEquals(setOf(french), awaitItem())

                store.setFilters(setOf(SessionFilter(SessionFilterType.LANGUAGE, "FRENCH")))

                expectNoEvents()
            }
        }

    @Test
    fun setting_a_different_set_emits_it() =
        runTest {
            val store = FakeSessionFiltersStore()

            store.filters.test {
                assertEquals(emptySet(), awaitItem())

                store.setFilters(setOf(titan))

                assertEquals(setOf(titan), awaitItem())
            }
        }
}
