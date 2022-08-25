package com.gdgnantes.devfest.android.utils

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat

fun String.titlecaseFirstCharIfItIsLowercase() = replaceFirstChar {
    val locale = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0)
    if (locale != null && it.isLowerCase()) it.titlecase(locale) else it.toString()
}