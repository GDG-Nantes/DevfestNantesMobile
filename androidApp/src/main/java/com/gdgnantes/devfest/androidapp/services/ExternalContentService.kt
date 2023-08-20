package com.gdgnantes.devfest.androidapp.services

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.gdgnantes.devfest.androidapp.R
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
            launchTab(url)
        }
    }

    private fun launchTab(url: String) {
        try {
            val parse = Uri.parse(url)
            val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(
                            ContextCompat.getColor(activity, R.color.md_theme_primary)
                        ).build()
                )
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(activity, parse)
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
            .setPositiveButton(R.string.btn_ok) { _, _ -> launchTab(url) }
            .setNegativeButton(R.string.btn_cancel, null)
            .create()
            .show()
    }

    companion object {
        private const val URL_GDG_NANTES = "gdgnantes.com"
    }
}