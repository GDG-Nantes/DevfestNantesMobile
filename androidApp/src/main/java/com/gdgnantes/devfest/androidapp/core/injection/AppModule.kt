package com.gdgnantes.devfest.androidapp.core.injection

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.DataSharingInitializer
import com.gdgnantes.devfest.androidapp.core.OpenFeedbackInitializer
import com.gdgnantes.devfest.androidapp.core.logging.TimberTreeDebug
import com.gdgnantes.devfest.androidapp.core.logging.TimberTreeRelease
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceInitializer
import com.gdgnantes.devfest.androidapp.ui.screens.session.OpenFeedbackConfig
import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.analytics.FirebaseAnalyticsService
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.BookmarksStoreImpl
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.DevFestNantesStoreBuilder
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersServiceImpl
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsServiceImpl
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

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

    @AppScope
    @Binds
    abstract fun filtersService(impl: SessionFiltersServiceImpl): SessionFiltersService

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
            openFeedbackInitializer: OpenFeedbackInitializer,
            performanceInitializer: PerformanceInitializer
        ): Set<ApplicationInitializer> =
            setOf(
                dataSharingInitializer,
                openFeedbackInitializer,
                performanceInitializer
            )

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

        @Provides
        fun firebasePerformance() = FirebasePerformance.getInstance()

        @Provides
        fun openFeedbackConfig() =
            OpenFeedbackConfig(
                enabled = BuildConfig.OPEN_FEEDBACK_ENABLED.toBoolean(),
                projectId = BuildConfig.OPEN_FEEDBACK_PROJECT_ID
            )

        @AppScope
        @Provides
        fun providePerformanceMonitoring(firebasePerformance: FirebasePerformance): PerformanceMonitoring {
            return PerformanceMonitoring(firebasePerformance)
        }

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

        @AppScope
        @Provides
        fun timberTree(
            timberTreeDebug: TimberTreeDebug,
            timberTreeRelease: TimberTreeRelease
        ): Timber.Tree {
            return if (BuildConfig.DEBUG) {
                timberTreeDebug
            } else {
                timberTreeRelease
            }
        }
    }
}
