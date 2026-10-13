package com.gdgnantes.devfest.androidapp

import android.app.Application
import com.gdgnantes.devfest.androidapp.core.ApplicationInitializer
import com.gdgnantes.devfest.androidapp.core.CoroutinesDispatcherProvider
import com.gdgnantes.devfest.androidapp.core.performance.PerformanceInitializer
import com.gdgnantes.devfest.androidapp.di.androidModules
import com.gdgnantes.devfest.shared.di.initKoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import timber.log.Timber

class DevFestNantesApplication : Application() {
    // Resolved lazily: the initializer list is first touched inside the main-dispatcher coroutine below.
    private val appInitializers: List<ApplicationInitializer> by inject(named("applicationInitializers"))
    private val coroutineDispatcherProvider: CoroutinesDispatcherProvider by inject()
    private val timerTree: Timber.Tree by inject()

    lateinit var coroutineScope: CoroutineScope

    override fun onCreate() {
        // Start performance monitoring as early as possible
        PerformanceInitializer.startAppStartupTrace()

        // Koin must be running before anything resolves a dependency through `by inject()`.
        initKoin(platformModules = androidModules) { androidContext(this@DevFestNantesApplication) }

        super.onCreate()

        Timber.plant(timerTree)

        coroutineScope = CoroutineScope(coroutineDispatcherProvider.main)

        coroutineScope.launch {
            appInitializers.forEach { initializer ->
                initializer()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        coroutineScope.coroutineContext.cancelChildren()
    }
}
