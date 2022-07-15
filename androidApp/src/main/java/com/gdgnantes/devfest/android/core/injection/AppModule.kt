package com.gdgnantes.devfest.android.core.injection

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
        fun store(): DevFestNantesStore = DevFestNantesStoreMocked()
    }
}
