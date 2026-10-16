package com.gdgnantes.devfest.feature.sessiondetail.utils

import android.content.Context
import android.content.res.Resources
import android.text.format.DateUtils
import androidx.core.os.ConfigurationCompat
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.utils.toInstantOrNull
import com.gdgnantes.devfest.core.ui.utils.titlecaseFirstCharIfItIsLowercase
import java.util.Formatter
import java.util.TimeZone

fun ScheduleSlot.getFormattedRange(context: Context): String {
    val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    val start = startDate.toInstantOrNull()?.toEpochMilliseconds()
    val end = endDate.toInstantOrNull()?.toEpochMilliseconds()
    return if (start == null || end == null) {
        ""
    } else {
        DateUtils.formatDateRange(
            context,
            Formatter(locale),
            start,
            end,
            DateUtils.FORMAT_SHOW_WEEKDAY or
                    DateUtils.FORMAT_SHOW_TIME or
                    DateUtils.FORMAT_SHOW_DATE or
                    DateUtils.FORMAT_SHOW_YEAR,
            TimeZone.getDefault().id
        ).toString().titlecaseFirstCharIfItIsLowercase()
    }
}
