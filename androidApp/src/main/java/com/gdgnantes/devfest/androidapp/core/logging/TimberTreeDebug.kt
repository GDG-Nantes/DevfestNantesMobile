package com.gdgnantes.devfest.androidapp.core.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import javax.inject.Inject

class TimberTreeDebug @Inject constructor(private val firebaseCrashlytics: FirebaseCrashlytics) :
    Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.ERROR || priority == Log.WARN) {
            if (t != null) {
                firebaseCrashlytics.recordException(t)
            } else {
                firebaseCrashlytics.log(message)
            }
        }
        super.log(priority, tag, message, t)
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        return super.createStackElementTag(element) + ':'.toString() + element.lineNumber
    }
}
