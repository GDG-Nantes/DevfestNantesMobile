package com.gdgnantes.devfest.core.analytics.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import timber.log.Timber

/**
 * Firebase Performance implementation of [PerformanceMonitoring].
 */
@Suppress("TooGenericExceptionCaught")
class FirebasePerformanceMonitoring(
    private val firebasePerformance: FirebasePerformance
) : PerformanceMonitoring {
    /**
     * Starts a custom trace for performance monitoring.
     * @param traceName The name of the trace
     * @return The Trace object to stop later
     */
    fun startTrace(traceName: String): Trace {
        return firebasePerformance.newTrace(traceName).apply {
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
            Timber.d("Stopped performance trace: $trace")
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

    override suspend fun <T> trackDataLoad(
        traceName: String,
        dataSource: String,
        itemCount: Int?,
        block: suspend () -> T
    ): T {
        val trace = startTrace(traceName)
        val startTime = System.currentTimeMillis()

        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime

            val attributes = mutableMapOf(PerformanceMonitoring.ATTR_DATA_SOURCE to dataSource)
            itemCount?.let { attributes["item_count"] = it.toString() }

            recordMetric(trace, "duration_ms", duration)
            stopTrace(trace, attributes)

            result
        } catch (e: Exception) {
            val attributes =
                mapOf(
                PerformanceMonitoring.ATTR_DATA_SOURCE to dataSource,
                PerformanceMonitoring.ATTR_ERROR_TYPE to e.javaClass.simpleName
            )
            stopTrace(trace, attributes)
            throw e
        }
    }

    override suspend fun <T> trace(
        traceName: String,
        attributes: Map<String, String>,
        block: suspend () -> T
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
}
