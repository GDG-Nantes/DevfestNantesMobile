package com.gdgnantes.devfest.feature.sessiondetail

import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import java.util.TimeZone
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * D-07 characterization tests for [getFallbackUrlForSession].
 *
 * They pin today's URL, including the `//` that comes from `OPENFEEDBACK_BASE_URL` already ending
 * with a slash, and MUST pass unchanged after the swap to the shared parser. The single deliberate
 * behavior change (D-05) is covered separately by `fallback_url_is_ascii_in_a_non_latin_digit_locale`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FallbackFeedbackUrlTest {
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

    private fun session(start: String, end: String, formId: String? = "form-1") =
        Session(
            id = "session-1",
            openFeedbackFormId = formId,
            scheduleSlot = ScheduleSlot(startDate = start, endDate = end)
        )

    private val morning =
        session("2025-10-16T11:00:00+02:00", "2025-10-16T11:45:00+02:00")

    @Test
    @Config(qualifiers = "en-rUS")
    fun fallback_url_en_us() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))

        assertEquals(
            "https://openfeedback.io/devfestnantes2025//2025-10-16/form-1",
            getFallbackUrlForSession(morning)
        )
    }

    @Test
    @Config(qualifiers = "fr-rFR")
    fun fallback_url_fr_fr() {
        Locale.setDefault(Locale.forLanguageTag("fr-FR"))

        assertEquals(
            "https://openfeedback.io/devfestnantes2025//2025-10-16/form-1",
            getFallbackUrlForSession(morning)
        )
    }

    @Test
    fun fallback_url_uses_the_local_date_in_paris() {
        val lateUtc = session("2025-10-16T23:30:00Z", "2025-10-17T00:15:00Z")

        assertEquals(
            "https://openfeedback.io/devfestnantes2025//2025-10-17/form-1",
            getFallbackUrlForSession(lateUtc)
        )
    }

    @Test
    fun fallback_url_uses_the_local_date_in_utc() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val lateUtc = session("2025-10-16T23:30:00Z", "2025-10-17T00:15:00Z")

        assertEquals(
            "https://openfeedback.io/devfestnantes2025//2025-10-16/form-1",
            getFallbackUrlForSession(lateUtc)
        )
    }

    @Test
    fun fallback_url_is_null_without_a_form_id() {
        assertNull(
            getFallbackUrlForSession(
                session("2025-10-16T11:00:00+02:00", "2025-10-16T11:45:00+02:00", formId = null)
            )
        )
    }
}
