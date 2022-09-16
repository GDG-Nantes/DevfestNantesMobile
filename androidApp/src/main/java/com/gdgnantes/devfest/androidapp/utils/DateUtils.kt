package com.gdgnantes.devfest.androidapp.utils

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.*

fun getDateFromIso8601(dateIso8601: String): Date? {
    val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", locale)
    dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
    return dateFormatter.parse(dateIso8601)
}