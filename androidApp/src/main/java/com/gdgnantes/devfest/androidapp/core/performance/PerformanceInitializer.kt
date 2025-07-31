package com.gdgnantes.devfest.androidapp.core.performance

import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import timber.log.Timber
import javax.inject.Inject

/**
 * Initializer that sets up performance monitoring for app startup.
 */
class PerformanceInitializer @Inject constructor() : ApplicationInitializer {
    companion object {
        private var appStartTrace: Trace? = null

        /**
         * Call this from Application.onCreate() to start tracking app startup.
         */
        fun startAppStartupTrace() {
            appStartTrace =
                FirebasePerformance.getInstance().newTrace("app_startup").apply {
                start()
                Timber.d("Started app startup trace")
            }
        }

        /**
         * Call this when the app is fully initialized to stop the startup trace.
         */
        fun stopAppStartupTrace() {
            appStartTrace?.let { trace ->
                trace.stop()
                Timber.d("Stopped app startup trace")
                appStartTrace = null
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun invoke(params: Unit?): Result<Unit> {
        return try {
            // Enable performance monitoring
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true

            // Stop the app startup trace since initialization is complete
            stopAppStartupTrace()

            Timber.d("Performance monitoring initialized with Android Firebase Performance")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize performance monitoring")
            Result.failure(e)
        }
    }
}
