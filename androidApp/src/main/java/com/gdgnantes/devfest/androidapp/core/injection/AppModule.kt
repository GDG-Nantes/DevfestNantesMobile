package com.gdgnantes.devfest.androidapp.core.injection

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.LoggerInitializer
import com.gdgnantes.devfest.androidapp.services.BookmarksStoreImpl
import com.gdgnantes.devfest.store.BookmarksStore
import com.gdgnantes.devfest.store.DevFestNantesStore
import com.gdgnantes.devfest.store.DevFestNantesStoreBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import io.openfeedback.android.OpenFeedback
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @AppScope
    @Binds
    abstract fun bookmarksStore(bookmarksStoreImpl: BookmarksStoreImpl): BookmarksStore

    companion object {
        @AppScope
        @Provides
        fun coroutinesDispatcherProvider() = CoroutinesDispatcherProvider(
            default = Dispatchers.Default,
            computation = Dispatchers.Default,
            io = Dispatchers.IO,
            main = Dispatchers.Main.immediate
        )

        @Provides
        @ElementsIntoSet
        fun applicationInitializers(
            loggerInitializer: LoggerInitializer
        ): Set<ApplicationInitializer> = setOf(loggerInitializer)

        @AppScope
        @Provides
        fun openFeedback(application: Application) =
            // Updates with DevFest Nantes' credentials.
            OpenFeedback(
                context = application,
                openFeedbackProjectId = BuildConfig.OPEN_FEEDBACK_PROJECT_ID,
                firebaseConfig = OpenFeedback.FirebaseConfig(
                    projectId = BuildConfig.OPEN_FEEDBACK_FIREBASE_PROJECT_ID,
                    applicationId = BuildConfig.OPEN_FEEDBACK_FIREBASE_APPLICATION_ID,
                    apiKey = BuildConfig.OPEN_FEEDBACK_FIREBASE_API_KEY,
                    databaseUrl = BuildConfig.OPEN_FEEDBACK_FIREBASE_DATABASE_URL
                )
            )

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
