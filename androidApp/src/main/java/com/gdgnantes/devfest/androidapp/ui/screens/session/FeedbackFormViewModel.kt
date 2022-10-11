package com.gdgnantes.devfest.androidapp.ui.screens.session

import androidx.lifecycle.ViewModel
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import io.openfeedback.android.OpenFeedback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FeedbackFormViewModel @Inject constructor(
    val openFeedback: OpenFeedback,
    private val remoteConfig: FirebaseRemoteConfig
) : ViewModel() {

    private val _isOpenFeedbackEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isOpenFeedbackEnabled: StateFlow<Boolean>
        get() = _isOpenFeedbackEnabled.asStateFlow()

    init {
        updateConfig()
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateConfig()
                    val updated = task.result
                    Timber.d("Config params updated: $updated")
                } else {
                    Timber.d("Config params could not be fetched")
                }
            }
    }

    private fun updateConfig() {
        _isOpenFeedbackEnabled.value =
            BuildConfig.OPEN_FEEDBACK_ENABLED.toBoolean() && remoteConfig.getBoolean("openfeedback_enabled")
    }
}