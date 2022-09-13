package com.gdgnantes.devfest.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.onNavigationClick(coordinates: String) {
    val uri = "geo:latitude,longtitude?q=$coordinates"
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
}