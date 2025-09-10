package com.gdgnantes.devfest.androidapp.core.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject

class TimberTreeRelease @Inject constructor(private val firebaseCrashlytics: FirebaseCrashlytics) :
    Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int) =
        !(priority == Log.VERBOSE || priority == Log.DEBUG)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isLoggable(tag, priority)) return
        Log.println(priority, tag ?: DEFAULT_TAG, message)
        if (priority == Log.ERROR || priority == Log.WARN) {
            if (t != null) {
                firebaseCrashlytics.recordException(t)
            } else {
                firebaseCrashlytics.log(message)
            }
        }
    }

    companion object {
        const val DEFAULT_TAG = "DevFestNantes"
    }
}
