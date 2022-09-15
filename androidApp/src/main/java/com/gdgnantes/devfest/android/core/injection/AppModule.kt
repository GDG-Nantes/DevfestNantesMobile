package com.gdgnantes.devfest.android.core.injection

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.gdgnantes.devfest.android.core.ApplicationInitializer
import com.gdgnantes.devfest.android.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.android.core.LoggerInitializer
import com.gdgnantes.devfest.android.services.BookmarksStoreImpl
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
                openFeedbackProjectId = "SECRET",
                firebaseConfig = OpenFeedback.FirebaseConfig(
                    projectId = "SECRET",
                    applicationId = "SECRET",
                    apiKey = "SECRET",
                    databaseUrl = "SECRET"
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
