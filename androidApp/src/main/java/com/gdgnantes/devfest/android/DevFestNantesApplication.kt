package com.gdgnantes.devfest.android

import android.app.Application
import com.gdgnantes.devfest.android.core.TimberTreeRelease
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class DevFestNantesApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initLoggers()
    }

    /**
     * Timber init
     */
    private fun initLoggers() {
        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return super.createStackElementTag(element) + ':'.toString() + element.lineNumber
                }
            })
        } else {
            Timber.plant(TimberTreeRelease())
        }
    }
}