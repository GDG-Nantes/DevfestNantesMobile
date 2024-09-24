package com.gdgnantes.devfest.androidapp.core

import android.app.Application
import com.gdgnantes.devfest.androidapp.BuildConfig
import io.openfeedback.viewmodels.OpenFeedbackFirebaseConfig
import io.openfeedback.viewmodels.initializeOpenFeedback
import javax.inject.Inject

class OpenFeedbackInitializer @Inject constructor(
    private val application: Application,
) : ApplicationInitializer {
    override suspend operator fun invoke(params: Unit?): Result<Unit> {
        initializeOpenFeedback(
            OpenFeedbackFirebaseConfig(
                context = application,
                projectId = BuildConfig.OPEN_FEEDBACK_FIREBASE_PROJECT_ID,
                applicationId = BuildConfig.OPEN_FEEDBACK_FIREBASE_APPLICATION_ID,
                apiKey = BuildConfig.OPEN_FEEDBACK_FIREBASE_API_KEY,
                databaseUrl = BuildConfig.OPEN_FEEDBACK_FIREBASE_DATABASE_URL
            )
        )
        return Result.success(Unit)
    }
}
