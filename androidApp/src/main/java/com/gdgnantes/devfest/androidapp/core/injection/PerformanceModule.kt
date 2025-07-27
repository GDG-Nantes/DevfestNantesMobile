package com.gdgnantes.devfest.androidapp.core.injection

import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceInitializer
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceMonitoring
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PerformanceModule {

    @Binds
    @IntoSet
    abstract fun bindPerformanceInitializer(performanceInitializer: PerformanceInitializer): ApplicationInitializer

    companion object {
        @Provides
        @Singleton
        fun providePerformanceMonitoring(): PerformanceMonitoring {
            return PerformanceMonitoring()
        }
    }
}
