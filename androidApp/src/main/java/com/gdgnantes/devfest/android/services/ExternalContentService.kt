package com.gdgnantes.devfest.android.services

import android.app.Activity
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class ExternalContentService @Inject constructor(private val activity: Activity) {
    fun openUrl(url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}