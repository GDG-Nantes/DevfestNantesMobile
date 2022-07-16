package com.gdgnantes.devfest.android

import android.app.Application
import com.gdgnantes.devfest.android.core.ApplicationInitializer
import com.gdgnantes.devfest.android.core.CoroutinesDispatcherProvider
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class DevFestNantesApplication: Application() {

    @Inject
    lateinit var appInitializers: Provider<Set<ApplicationInitializer>>

    @Inject
    lateinit var coroutineDispatcherProvider: CoroutinesDispatcherProvider

    lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        coroutineScope = CoroutineScope(coroutineDispatcherProvider.main)

        coroutineScope.launch {
            appInitializers.get().iterator().forEach { initializer ->
                initializer()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        coroutineScope.coroutineContext.cancelChildren()
    }
}