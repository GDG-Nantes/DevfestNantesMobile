package com.gdgnantes.devfest.androidapp.core.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized performance monitoring for the DevFest Nantes app.
 * Provides utilities for tracking custom performance metrics.
 */
@Singleton
class PerformanceMonitoring @Inject constructor() {

    companion object {
        // Custom trace names for key user flows
        const val TRACE_APP_STARTUP = "app_startup"
        const val TRACE_AGENDA_LOAD = "agenda_load"
        const val TRACE_SPEAKERS_LOAD = "speakers_load"
        const val TRACE_SESSION_DETAILS_LOAD = "session_details_load"
        const val TRACE_SPEAKER_DETAILS_LOAD = "speaker_details_load"
        const val TRACE_VENUE_LOAD = "venue_load"
        const val TRACE_PARTNERS_LOAD = "partners_load"
        
        // Custom attributes
        const val ATTR_DATA_SOURCE = "data_source"
        const val ATTR_SESSION_COUNT = "session_count"
        const val ATTR_SPEAKER_COUNT = "speaker_count"
        const val ATTR_ERROR_TYPE = "error_type"
    }

    /**
     * Starts a custom trace for performance monitoring.
     * @param traceName The name of the trace
     * @return The Trace object to stop later
     */
    fun startTrace(traceName: String): Trace {
        return FirebasePerformance.getInstance().newTrace(traceName).apply {
            start()
            Timber.d("Started performance trace: $traceName")
        }
    }

    /**
     * Stops a trace and optionally adds custom attributes.
     * @param trace The trace to stop
     * @param attributes Optional map of custom attributes to add
     */
    fun stopTrace(trace: Trace, attributes: Map<String, String> = emptyMap()) {
        try {
            attributes.forEach { (key, value) ->
                trace.putAttribute(key, value)
            }
            trace.stop()
            Timber.d("Stopped performance trace: ${trace}")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping performance trace")
        }
    }

    /**
     * Records a custom metric for the given trace.
     * @param trace The trace to add the metric to
     * @param metricName The name of the metric
     * @param value The metric value
     */
    fun recordMetric(trace: Trace, metricName: String, value: Long) {
        try {
            trace.putMetric(metricName, value)
            Timber.d("Recorded metric $metricName: $value")
        } catch (e: Exception) {
            Timber.e(e, "Error recording metric: $metricName")
        }
    }

    /**
     * Convenience method to track data loading performance.
     * @param traceName The name of the trace
     * @param dataSource The source of the data (e.g., "graphql", "cache")
     * @param itemCount Number of items loaded
     * @param block The code block to execute and measure
     */
    suspend fun <T> trackDataLoad(
        traceName: String,
        dataSource: String,
        itemCount: Int? = null,
        block: suspend () -> T
    ): T {
        val trace = startTrace(traceName)
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            
            val attributes = mutableMapOf(ATTR_DATA_SOURCE to dataSource)
            itemCount?.let { attributes["item_count"] = it.toString() }
            
            recordMetric(trace, "duration_ms", duration)
            stopTrace(trace, attributes)
            
            result
        } catch (e: Exception) {
            val attributes = mapOf(
                ATTR_DATA_SOURCE to dataSource,
                ATTR_ERROR_TYPE to e.javaClass.simpleName
            )
            stopTrace(trace, attributes)
            throw e
        }
    }

    /**
     * Tracks screen navigation performance.
     * @param screenName The name of the screen being navigated to
     * @param block The navigation code to execute
     */
    suspend fun <T> trackScreenNavigation(
        screenName: String,
        block: suspend () -> T
    ): T {
        val trace = startTrace("screen_navigation_$screenName")
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            
            recordMetric(trace, "navigation_duration_ms", duration)
            stopTrace(trace, mapOf("screen_name" to screenName))
            
            result
        } catch (e: Exception) {
            stopTrace(trace, mapOf(
                "screen_name" to screenName,
                ATTR_ERROR_TYPE to e.javaClass.simpleName
            ))
            throw e
        }
    }
}

/**
 * Extension function to easily track code blocks with performance monitoring.
 */
suspend inline fun <T> PerformanceMonitoring.trace(
    traceName: String,
    attributes: Map<String, String> = emptyMap(),
    crossinline block: suspend () -> T
): T {
    val trace = startTrace(traceName)
    val startTime = System.currentTimeMillis()
    
    return try {
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        
        recordMetric(trace, "duration_ms", duration)
        stopTrace(trace, attributes)
        
        result
    } catch (e: Exception) {
        val errorAttributes = attributes + mapOf(PerformanceMonitoring.ATTR_ERROR_TYPE to e.javaClass.simpleName)
        stopTrace(trace, errorAttributes)
        throw e
    }
}
