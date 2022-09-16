package com.gdgnantes.devfest.androidapp.utils

import android.content.Context
import android.content.res.Resources
import android.text.format.DateUtils
import androidx.core.os.ConfigurationCompat
import com.gdgnantes.devfest.model.ScheduleSlot
import java.util.*

fun ScheduleSlot.getFormattedRange(context: Context): String {
    val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    val start = getDateFromIso8601(startDate)
    val end = getDateFromIso8601(endDate)
    return if (start == null || end == null) {
        ""
    } else {
        DateUtils.formatDateRange(
            context,
            Formatter(locale),
            start.time,
            end.time,
            DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR,
            TimeZone.getDefault().id
        ).toString().titlecaseFirstCharIfItIsLowercase()
    }
}