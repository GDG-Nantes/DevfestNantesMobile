package com.gdgnantes.devfest.android.core.injection

import android.app.Application
import com.gdgnantes.devfest.android.core.ApplicationInitializer
import com.gdgnantes.devfest.android.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.android.core.LoggerInitializer
import com.gdgnantes.devfest.store.DevFestNantesStore
import com.gdgnantes.devfest.store.DevFestNantesStoreMocked
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
        fun openfeedbac(application: Application) =
            OpenFeedback(
                context = application,
                openFeedbackProjectId = "mMHR63ARZQpPidFQISyc",
                firebaseConfig = OpenFeedback.FirebaseConfig(
                    projectId = "openfeedback-b7ab9",
                    applicationId = "1:765209934800:android:a6bb09f3deabc2277297d5",
                    apiKey = "AIzaSyC_cfbh8xKwF8UPxCeasGcsHyK4s5yZFeA",
                    databaseUrl = "https://openfeedback-b7ab9.firebaseio.com"
                )
            )

        @AppScope
        @Provides
        fun store(): DevFestNantesStore = DevFestNantesStoreMocked()
    }
}
