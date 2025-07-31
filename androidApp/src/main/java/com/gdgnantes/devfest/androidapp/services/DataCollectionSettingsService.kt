package com.gdgnantes.devfest.androidapp.services

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

interface DataCollectionSettingsService {
    val isDataCollectionAgreementSet: Boolean
    val dataCollectionServicesActivationStatus: Flow<Map<DataCollectionService, Boolean>>
    fun changeDataServiceActivationStatus(
        dataCollectionService: DataCollectionService,
        enabled: Boolean
    )

    fun consentToAllServices()
    fun disallowAllServices()
    fun updatesDataServicesActivationStatus()
}

class DataCollectionSettingsServiceImpl @Inject constructor(
    private val analyticsService: FirebaseAnalytics,
    private val sharedPreferences: SharedPreferences,
    private val firebaseCrashlytics: FirebaseCrashlytics,
    private val firebasePerformance: FirebasePerformance,
) : DataCollectionSettingsService {
    private val enabledDataServices: MutableSet<String> = HashSet()

    override val isDataCollectionAgreementSet: Boolean
        get() = sharedPreferences.contains(SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES)

    private val _dataCollectionServicesActivationStatus: MutableStateFlow<Map<DataCollectionService, Boolean>>
    override val dataCollectionServicesActivationStatus: StateFlow<Map<DataCollectionService, Boolean>>
        get() = _dataCollectionServicesActivationStatus.asStateFlow()

    init {
        val prefSet =
            sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES, null)
        if (prefSet != null) {
            enabledDataServices.addAll(prefSet)
        }

        _dataCollectionServicesActivationStatus =
            MutableStateFlow(
            buildDataCollectionToolsList()
        )
    }

    override fun changeDataServiceActivationStatus(
        dataCollectionService: DataCollectionService,
        enabled: Boolean
    ) {
        if (enabled) {
            enabledDataServices.add(dataCollectionService.name)
        } else {
            enabledDataServices.remove(dataCollectionService.name)
        }

        synchronise()
    }

    override fun consentToAllServices() {
        DataCollectionService.values().map { it.name }.toSet().let {
            enabledDataServices.addAll(it)
        }
        synchronise()
    }

    override fun disallowAllServices() {
        enabledDataServices.clear()
        synchronise()
    }

    private fun synchronise() {
        _dataCollectionServicesActivationStatus.value = buildDataCollectionToolsList()
        updatesDataServicesActivationStatus()
        save()
    }

    private fun save() {
        sharedPreferences.edit {
            putStringSet(SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES, enabledDataServices)
            apply()
        }
    }

    override fun updatesDataServicesActivationStatus() {
        updatesFirebaseActivationStatusCrashlytics()
        updatesFirebasePerformanceActivationStatus()
        updatesGoogleAnalyticsActivationStatusCrashlytics()
    }

    private fun updatesFirebaseActivationStatusCrashlytics() {
        firebaseCrashlytics.isCrashlyticsCollectionEnabled =
            enabledDataServices.contains(
                DataCollectionService.FIREBASE_CRASHLYTICS.name
        )
    }

    private fun updatesFirebasePerformanceActivationStatus() {
        firebasePerformance.isPerformanceCollectionEnabled =
            enabledDataServices.contains(DataCollectionService.FIREBASE_PERFORMANCE.name)
    }

    private fun updatesGoogleAnalyticsActivationStatusCrashlytics() {
        analyticsService.setAnalyticsCollectionEnabled(
            enabledDataServices.contains(
                DataCollectionService.GOOGLE_ANALYTICS.name
            )
        )
    }

    private fun buildDataCollectionToolsList(): Map<DataCollectionService, Boolean> {
        return DataCollectionService.values().associateWith { dataSharingService ->
            enabledDataServices.contains(dataSharingService.name)
        }
    }

    companion object {
        private const val SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES =
            "SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS"
    }
}

enum class DataCollectionService {
    GOOGLE_ANALYTICS,
    FIREBASE_CRASHLYTICS,
    FIREBASE_PERFORMANCE,
}
