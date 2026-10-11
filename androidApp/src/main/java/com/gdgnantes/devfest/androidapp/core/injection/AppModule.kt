package com.gdgnantes.devfest.androidapp.core.injection

import android.content.SharedPreferences
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.DataSharingInitializer
import com.gdgnantes.devfest.androidapp.core.OpenFeedbackInitializer
import com.gdgnantes.devfest.androidapp.core.logging.TimberTreeDebug
import com.gdgnantes.devfest.androidapp.core.logging.TimberTreeRelease
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceInitializer
import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.feature.agenda.services.SessionFiltersService
import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import kotlinx.coroutines.Dispatchers
import org.koin.mp.KoinPlatform
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
// Temporary Hilt-to-Koin bridges inflate the provider count; AppModule disappears with Hilt (04-07).
@Suppress("TooManyFunctions")
object AppModule {
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

    @AppScope
    @Provides
    fun bookmarksStore(): BookmarksStore = KoinPlatform.getKoin().get()

    @AppScope
    @Provides
    fun analyticsService(): AnalyticsService = KoinPlatform.getKoin().get()

    @Provides
    fun analytics(): FirebaseAnalytics = KoinPlatform.getKoin().get()

    @Provides
    fun crashlytics(): FirebaseCrashlytics = KoinPlatform.getKoin().get()

    // DataCollectionSettingsService is stateful: the Koin single is the only instance (D-06).
    @AppScope
    @Provides
    fun dataSharingSettingsService(): DataCollectionSettingsService = KoinPlatform.getKoin().get()

    // SessionFiltersService is stateful: the Koin single is the only instance, shared with the
    // agenda screen (D-06).
    @AppScope
    @Provides
    fun filtersService(): SessionFiltersService = KoinPlatform.getKoin().get()

    @Provides
    fun firebasePerformance(): FirebasePerformance = KoinPlatform.getKoin().get()

    @AppScope
    @Provides
    fun providePerformanceMonitoring(): PerformanceMonitoring = KoinPlatform.getKoin().get()

    @AppScope
    @Provides
    fun sharedPreferences(): SharedPreferences = KoinPlatform.getKoin().get()

    @AppScope
    @Provides
    fun store(): DevFestNantesStore {
        return KoinPlatform.getKoin().get()
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
