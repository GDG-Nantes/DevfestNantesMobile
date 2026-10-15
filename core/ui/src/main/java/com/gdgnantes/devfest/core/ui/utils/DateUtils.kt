package com.gdgnantes.devfest.core.ui.utils

import android.content.Context
import android.text.format.DateUtils
import com.gdgnantes.devfest.core.model.utils.toInstantOrNull
import java.util.Date

fun getDateFromIso8601(dateIso8601: String): Date? =
    dateIso8601.toInstantOrNull()?.let { Date(it.toEpochMilliseconds()) }

fun getDayFromIso8601(dateIso8601: String, context: Context): String? {
    return dateIso8601.toInstantOrNull()?.toEpochMilliseconds()?.let { millis ->
        DateUtils.formatDateTime(
            context,
            millis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
        ).toString().titlecaseFirstCharIfItIsLowercase()
    }
}
