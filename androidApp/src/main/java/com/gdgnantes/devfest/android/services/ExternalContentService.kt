package com.gdgnantes.devfest.android.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.gdgnantes.devfest.android.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

@ActivityScoped
class ExternalContentService @Inject constructor(
    private val activity: Activity
) {
    fun openUrl(url: String) {
        if (!isOwnerWebsite(url)) {
            showExternalDisclaimer(url)
        } else {
            launchTab(activity, url)
        }
    }

    private fun launchTab(context: Context, url: String) {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        //builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
        builder.setShowTitle(true)
        val customTabsIntent: CustomTabsIntent = builder.build()
        try {
            val parse = Uri.parse(url)
            customTabsIntent.launchUrl(context, parse)
        } catch (exception: Exception) {
            Timber.e(exception)
        }
    }

    private fun isOwnerWebsite(stringUrl: String): Boolean {
        return try {
            val url = URL(stringUrl)
            val host = url.host
            host.contains(URL_GDG_NANTES)
        } catch (e: MalformedURLException) {
            false
        }
    }

    private fun showExternalDisclaimer(url: String) {
        MaterialAlertDialogBuilder(activity)
            .setMessage(R.string.web_external_disclaimer_text)
            .setTitle(R.string.web_external_disclaimer_title)
            .setPositiveButton(R.string.btn_ok) { _, _ -> openDefaultBrowser(url) }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
            .show()
    }

    fun openDefaultBrowser(url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    companion object {
        private const val URL_GDG_NANTES = "gdgnantes.com"
    }
}