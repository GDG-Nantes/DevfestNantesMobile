package com.gdgnantes.devfest.feature.sessiondetail.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.core.model.ScheduleSlot
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale
import java.util.TimeZone
import kotlin.test.assertEquals

/**
 * D-07 characterization tests for [getFormattedRange].
 *
 * They were written green against the `SimpleDateFormat`-based `getDateFromIso8601` and MUST pass
 * unchanged after the swap to the shared `kotlin.time.Instant` parser. A [ScheduleSlot] with an
 * unparseable date cannot be built (its initializer throws), so there is no null-path case.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ScheduleSlotFormattedRangeTest {
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

    private val slot =
        ScheduleSlot(
            startDate = "2025-10-16T11:00:00+02:00",
            endDate = "2025-10-16T11:45:00+02:00"
        )

    @Test
    @Config(qualifiers = "en-rUS")
    fun formatted_range_en_us() {
        Locale.setDefault(Locale.forLanguageTag("en-US"))
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals(
            "Thursday, October 16, 2025, 11:00\u2009\u2013\u200911:45\u202fAM",
            slot.getFormattedRange(context)
        )
    }

    @Test
    @Config(qualifiers = "fr-rFR")
    fun formatted_range_fr_fr() {
        Locale.setDefault(Locale.forLanguageTag("fr-FR"))
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals(
            "Jeudi 16 octobre 2025, 11:00\u2009\u2013\u200911:45",
            slot.getFormattedRange(context)
        )
    }
}
