package com.gdgnantes.devfest.core.model.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for the shared ISO-8601 helpers (D-06). Expected epoch values are absolute and computed
 * independently from the input strings. Runs on JVM and iOS.
 */
class IsoDateTest {

    @Test
    fun offset_form_parses_to_expected_epoch_milliseconds() {
        assertEquals(1760605200000L, "2025-10-16T11:00:00+02:00".toInstantOrNull()?.toEpochMilliseconds())
    }

    @Test
    fun utc_z_form_parses_to_expected_epoch_milliseconds() {
        assertEquals(1760605200000L, "2025-10-16T09:00:00Z".toInstantOrNull()?.toEpochMilliseconds())
    }

    @Test
    fun empty_input_returns_null() {
        assertNull("".toInstantOrNull())
    }

    @Test
    fun garbage_input_returns_null() {
        assertNull("not-a-date".toInstantOrNull())
    }

    @Test
    fun date_only_input_returns_null() {
        assertNull("2025-10-16".toInstantOrNull())
    }

    @Test
    fun zone_less_input_returns_null() {
        assertNull("2025-10-16T09:00:00".toInstantOrNull())
    }

    @Test
    fun late_evening_utc_instant_is_the_same_local_date_in_utc() {
        val instant = assertNotNull("2025-10-16T23:30:00Z".toInstantOrNull())

        assertEquals(LocalDate(2025, 10, 16), instant.toLocalDate(TimeZone.UTC))
    }

    @Test
    fun late_evening_utc_instant_is_the_next_local_date_in_paris() {
        val instant = assertNotNull("2025-10-16T23:30:00Z".toInstantOrNull())

        assertEquals(LocalDate(2025, 10, 17), instant.toLocalDate(TimeZone.of("Europe/Paris")))
    }

    @Test
    fun local_date_renders_as_ascii_iso_text() {
        assertEquals("2025-10-16", LocalDate(2025, 10, 16).toString())
    }
}
