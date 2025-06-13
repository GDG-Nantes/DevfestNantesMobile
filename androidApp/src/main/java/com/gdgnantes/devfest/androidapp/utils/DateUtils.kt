package com.gdgnantes.devfest.androidapp.utils

import android.content.Context
import android.content.res.Resources
import android.text.format.DateUtils
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

fun getDateFromIso8601(dateIso8601: String): Date? {
    val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    // Try parsing with timezone offset first
    val formats =
        listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX", // e.g. 2024-10-17T08:00:00+02:00
            "yyyy-MM-dd'T'HH:mm:ss'Z'" // legacy UTC
        )
    for (pattern in formats) {
        try {
            val dateFormatter =
                SimpleDateFormat(
                    pattern,
                    locale
                )
            if (pattern.endsWith("'Z'")) {
                dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
            }
            return dateFormatter.parse(dateIso8601)
        } catch (_: Exception) {
            // Try next pattern
        }
    }
    return null // Unparseable
}

fun getDayFromIso8601(dateIso8601: String, context: Context): String? {
    return getDateFromIso8601(dateIso8601)?.run {
        DateUtils.formatDateTime(
            context,
            time,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
        ).toString().titlecaseFirstCharIfItIsLowercase()
    }
}
