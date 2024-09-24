package com.gdgnantes.devfest.androidapp.core.injection

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdgnantes.devfest.analytics.AnalyticsService
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.DataSharingInitializer
import com.gdgnantes.devfest.androidapp.core.LoggerInitializer
import com.gdgnantes.devfest.androidapp.core.OpenFeedbackInitializer
import com.gdgnantes.devfest.androidapp.services.BookmarksStoreImpl
import com.gdgnantes.devfest.androidapp.services.DataCollectionSettingsService
import com.gdgnantes.devfest.androidapp.services.DataCollectionSettingsServiceImpl
import com.gdgnantes.devfest.androidapp.services.FirebaseAnalyticsService
import com.gdgnantes.devfest.store.BookmarksStore
import com.gdgnantes.devfest.store.DevFestNantesStore
import com.gdgnantes.devfest.store.DevFestNantesStoreBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @AppScope
    @Binds
    abstract fun analyticsService(firebaseAnalyticsService: FirebaseAnalyticsService): AnalyticsService

    @AppScope
    @Binds
    abstract fun bookmarksStore(bookmarksStoreImpl: BookmarksStoreImpl): BookmarksStore

    @AppScope
    @Binds
    abstract fun dataSharingSettingsService(
        dataCollectionSettingsServiceImpl: DataCollectionSettingsServiceImpl
    ): DataCollectionSettingsService

    companion object {
        const val REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL = 300L

        @AppScope
        @Provides
        fun coroutinesDispatcherProvider() =
            CoroutinesDispatcherProvider(
                default = Dispatchers.Default,
                computation = Dispatchers.Default,
                io = Dispatchers.IO,
                main = Dispatchers.Main.immediate
            )

        @Provides
        @ElementsIntoSet
        fun applicationInitializers(
            dataSharingInitializer: DataSharingInitializer,
            loggerInitializer: LoggerInitializer,
            openFeedbackInitializer: OpenFeedbackInitializer
        ): Set<ApplicationInitializer> =
            setOf(dataSharingInitializer, loggerInitializer, openFeedbackInitializer)

        @Provides
        fun analytics() = Firebase.analytics

        @Provides
        fun config() =
            Firebase.remoteConfig
                .apply {
                    val configSettings =
                        remoteConfigSettings {
                            minimumFetchIntervalInSeconds = REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL
                        }
                    setConfigSettingsAsync(configSettings)
                }

        @Provides
        fun crashlytics() = FirebaseCrashlytics.getInstance()

        @AppScope
        @Provides
        fun sharedPreferences(application: Application): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(application)
        }

        @AppScope
        @Provides
        fun store(): DevFestNantesStore {
            return DevFestNantesStoreBuilder()
                .setUseMockServer(false)
                .build()
        }
    }
}
