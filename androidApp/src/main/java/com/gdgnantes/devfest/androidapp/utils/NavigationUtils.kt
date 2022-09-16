package com.gdgnantes.devfest.androidapp.utils

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri

fun Context.onNavigationClick(location: Location) {
    val uri = "geo:latitude,longtitude?q= ${location.latitude},${location.longitude}"
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
}