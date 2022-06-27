package com.gdgnantes.devfest.android.core.injection

import com.gdgnantes.devfest.kotlinmultiplatformsharedmodule.DevFestNantesStoreMocked
import com.gdgnantes.devfest.store.DevFestNantesStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    companion object {
        @AppScope
        @Provides
        fun store(): DevFestNantesStore = DevFestNantesStoreMocked()
    }
}
