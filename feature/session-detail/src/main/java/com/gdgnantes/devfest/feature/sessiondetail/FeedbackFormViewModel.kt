package com.gdgnantes.devfest.feature.sessiondetail

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class FeedbackFormViewModel(
    private val feedbackFlags: FeedbackFlags,
    private val openFeedbackConfig: OpenFeedbackConfig
) : ViewModel() {
    private val _isOpenFeedbackEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isOpenFeedbackEnabled: StateFlow<Boolean>
        get() = _isOpenFeedbackEnabled.asStateFlow()

    private val _isOpenfeedbackFallbackRequested: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    val isOpenfeedbackFallbackRequested: StateFlow<Boolean>
        get() = _isOpenfeedbackFallbackRequested.asStateFlow()

    val openFeedbackProjectId: String
        get() = openFeedbackConfig.projectId

    init {
        updateConfig()
        feedbackFlags.fetchAndActivate { successful, activated ->
            if (successful) {
                updateConfig()
                Timber.d("Config params updated: $activated")
            } else {
                Timber.d("Config params could not be fetched")
            }
        }
    }

    private fun updateConfig() {
        _isOpenFeedbackEnabled.value =
            openFeedbackConfig.enabled &&
                    feedbackFlags.getBoolean("openfeedback_enabled")
        _isOpenfeedbackFallbackRequested.value =
            openFeedbackConfig.enabled &&
                    feedbackFlags.getBoolean("openfeedback_fallback_requested_android")
    }
}
