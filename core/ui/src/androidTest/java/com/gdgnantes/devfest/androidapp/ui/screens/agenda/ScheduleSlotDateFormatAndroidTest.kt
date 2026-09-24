package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gdgnantes.devfest.androidapp.utils.getDateFromIso8601
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ScheduleSlotDateFormatAndroidTest {
    @Test
    fun scheduleSlot_dates_are_parseable_by_android_java() {
        // Example of a date string produced by the backend
        val dateStrings = listOf(
            "2024-10-17T08:00:00+02:00",
            "2024-10-17T09:00:00+02:00"
        )
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        for (date in dateStrings) {
            try {
                format.parse(date)
            } catch (e: Exception) {
                throw AssertionError("Unparseable date: $date", e)
            }
        }
    }

    @Test
    fun scheduleSlot_dates_are_parseable_by_getDateFromIso8601() {
        val dateStrings = listOf(
            "2024-10-17T08:00:00+02:00",
            "2024-10-17T09:00:00+02:00"
        )
        for (date in dateStrings) {
            try {
                getDateFromIso8601(date) ?: throw AssertionError("Null date for: $date")
            } catch (e: Exception) {
                throw AssertionError("Unparseable date by getDateFromIso8601: $date", e)
            }
        }
    }
}
