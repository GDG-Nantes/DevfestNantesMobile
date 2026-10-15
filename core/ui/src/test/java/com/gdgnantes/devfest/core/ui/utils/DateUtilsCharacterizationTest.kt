package com.gdgnantes.devfest.core.ui.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.core.model.Agenda
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * D-07 characterization tests for [getDateFromIso8601] and [getDayFromIso8601].
 *
 * These pin today's parse results (absolute epoch millis, computed independently of the code under
 * test) and the localized day labels. They were written green against the `SimpleDateFormat`
 * implementation and MUST pass unchanged after the swap to the shared `kotlin.time.Instant` parser.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DateUtilsCharacterizationTest {
    private lateinit var savedTimeZone: TimeZone
    private lateinit var savedLocale: Locale

    @Before
    fun setUp() {
        savedTimeZone = TimeZone.getDefault()
        savedLocale = Locale.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(savedTimeZone)
        Locale.setDefault(savedLocale)
    }

    private fun millisOf(iso: String): Long? = getDateFromIso8601(iso)?.time

    @Test
    fun offset_form_parses_to_absolute_epoch_millis() {
        assertEquals(1760605200000L, millisOf("2025-10-16T11:00:00+02:00"))
    }

    @Test
    fun utc_z_form_parses_to_absolute_epoch_millis() {
        assertEquals(1760605200000L, millisOf("2025-10-16T09:00:00Z"))
    }

    @Test
    fun agenda_day_constants_parse_to_midnight_utc() {
        assertEquals(1760572800000L, millisOf(Agenda.DAY_ONE_ISO))
        assertEquals(1760659200000L, millisOf(Agenda.DAY_TWO_ISO))
    }

    @Test
    fun late_evening_offset_form_parses_to_utc_instant() {
        assertEquals(1760650200000L, millisOf("2025-10-16T23:30:00+02:00"))
    }

    @Test
    fun legacy_previous_year_offset_form_parses() {
        assertEquals(1729144800000L, millisOf("2024-10-17T08:00:00+02:00"))
    }

    @Test
    fun unparseable_inputs_return_null() {
        assertNull(getDateFromIso8601(""))
        assertNull(getDateFromIso8601("not-a-date"))
        assertNull(getDateFromIso8601("2025-10-16"))
        assertNull(getDateFromIso8601("2025-10-16T09:00:00"))
    }

    @Test
    @Config(qualifiers = "en-rUS")
    fun day_label_en_us() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals("October 16, 2025", getDayFromIso8601(Agenda.DAY_ONE_ISO, context))
        assertEquals("October 17, 2025", getDayFromIso8601(Agenda.DAY_TWO_ISO, context))
    }

    @Test
    @Config(qualifiers = "fr-rFR")
    fun day_label_fr_fr() {
        Locale.setDefault(Locale.forLanguageTag("fr-FR"))
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals("16 octobre 2025", getDayFromIso8601(Agenda.DAY_ONE_ISO, context))
        assertEquals("17 octobre 2025", getDayFromIso8601(Agenda.DAY_TWO_ISO, context))
    }

    @Test
    fun day_label_is_null_for_unparseable_input() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertNull(getDayFromIso8601("not-a-date", context))
        assertNotNull(getDayFromIso8601(Agenda.DAY_ONE_ISO, context))
    }
}
