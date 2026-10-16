package com.gdgnantes.devfest.feature.sessiondetail

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

/**
 * The remote flags that gate the feedback form. An interface so [FeedbackFormViewModel] and its
 * tests depend on a seam rather than on Firebase Remote Config (D-23); the production
 * implementation is [FirebaseFeedbackFlags].
 */
interface FeedbackFlags {
    /** Current value of the boolean flag [key]; a missing key reads as `false`. */
    fun getBoolean(key: String): Boolean

    /**
     * Fetches and activates the remote values, then calls [onComplete] with whether the fetch
     * succeeded and whether new values were activated. [activated] is `false` when the fetch failed.
     */
    fun fetchAndActivate(onComplete: (successful: Boolean, activated: Boolean) -> Unit)
}

/** [FeedbackFlags] backed by Firebase Remote Config. */
class FirebaseFeedbackFlags(
    private val remoteConfig: FirebaseRemoteConfig
) : FeedbackFlags {
    override fun getBoolean(key: String): Boolean = remoteConfig.getBoolean(key)

    override fun fetchAndActivate(onComplete: (successful: Boolean, activated: Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                // Reading the result of a failed task throws, so it is only read on success.
                onComplete(task.isSuccessful, task.isSuccessful && task.result == true)
            }
    }
}
