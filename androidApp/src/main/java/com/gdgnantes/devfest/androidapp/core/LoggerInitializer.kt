package com.gdgnantes.devfest.androidapp.core

import com.gdgnantes.devfest.androidapp.BuildConfig
import timber.log.Timber
import javax.inject.Inject

class LoggerInitializer @Inject constructor() : ApplicationInitializer {
    override suspend operator fun invoke(params: Unit?): Result<Unit> {
        if (BuildConfig.DEBUG) {
            Timber.plant(
                object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return super.createStackElementTag(element) + ':'.toString() + element.lineNumber
                }
                }
            )
        } else {
            Timber.plant(TimberTreeRelease())
        }
        return Result.success(Unit)
    }
}
