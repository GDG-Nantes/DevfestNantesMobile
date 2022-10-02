package com.gdgnantes.devfest.androidapp.services

import android.content.SharedPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

interface DataSharingSettingsService {
    val isDataSharingAgreementSet: Boolean
    val dataSharingServicesActivationStatus: Flow<Map<DataSharingService, Boolean>>
    fun changeDataServiceActivationStatus(dataSharingService: DataSharingService, enabled: Boolean)
    fun consentToAllServices()
    fun disallowAllServices()
    fun updatesDataServicesActivationStatus()
}

class DataSharingSettingsServiceImpl @Inject constructor(
    private val analyticsService: FirebaseAnalytics,
    private val sharedPreferences: SharedPreferences,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : DataSharingSettingsService {

    private val enabledDataServices: MutableSet<String> = HashSet()

    override val isDataSharingAgreementSet: Boolean
        get() = sharedPreferences.contains(SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES)

    private val _dataSharingServicesActivationStatus: MutableStateFlow<Map<DataSharingService, Boolean>>
    override val dataSharingServicesActivationStatus: StateFlow<Map<DataSharingService, Boolean>>
        get() = _dataSharingServicesActivationStatus.asStateFlow()

    init {
        val prefSet =
            sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES, null)
        if (prefSet != null) {
            enabledDataServices.addAll(prefSet)
        }

        _dataSharingServicesActivationStatus = MutableStateFlow(
            buildDataSharingToolsList()
        )
    }

    override fun changeDataServiceActivationStatus(
        dataSharingService: DataSharingService,
        enabled: Boolean
    ) {
        if (enabled) {
            enabledDataServices.add(dataSharingService.name)
        } else {
            enabledDataServices.remove(dataSharingService.name)
        }

        synchronise()
    }

    override fun consentToAllServices() {
        DataSharingService.values().map { it.name }.toSet().let {
            enabledDataServices.addAll(it)
        }
        synchronise()
    }

    override fun disallowAllServices() {
        enabledDataServices.clear()
        synchronise()
    }

    private fun synchronise() {
        _dataSharingServicesActivationStatus.value = buildDataSharingToolsList()
        updatesDataServicesActivationStatus()
        save()
    }

    private fun save() {
        with(sharedPreferences.edit()) {
            putStringSet(SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES, enabledDataServices)
            apply()
        }
    }

    override fun updatesDataServicesActivationStatus() {
        updatesFirebaseActivationStatusCrashlytics()
        updatesGoogleAnalyticsActivationStatusCrashlytics()
    }

    private fun updatesFirebaseActivationStatusCrashlytics() {
        firebaseCrashlytics.setCrashlyticsCollectionEnabled(
            enabledDataServices.contains(
                DataSharingService.FIREBASE_CRASHLYTICS.name
            )
        )
    }

    private fun updatesGoogleAnalyticsActivationStatusCrashlytics() {
        analyticsService.setAnalyticsCollectionEnabled(
            enabledDataServices.contains(
                DataSharingService.GOOGLE_ANALYTICS.name
            )
        )
    }

    private fun buildDataSharingToolsList(): Map<DataSharingService, Boolean> {
        return DataSharingService.values().associateWith { dataSharingService ->
            enabledDataServices.contains(dataSharingService.name)
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES =
            "SHARED_PREFERENCES_KEY_ENABLED_DATASHARING_TOOLS"
    }

}

enum class DataSharingService {
    GOOGLE_ANALYTICS,
    FIREBASE_CRASHLYTICS
}