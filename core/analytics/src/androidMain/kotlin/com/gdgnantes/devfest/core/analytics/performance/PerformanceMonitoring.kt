package com.gdgnantes.devfest.core.analytics.performance

/**
 * Centralized performance monitoring for the DevFest Nantes app.
 * Provides utilities for tracking custom performance metrics.
 *
 * An interface so ViewModels and tests depend on a seam rather than on Firebase; the production
 * implementation is [FirebasePerformanceMonitoring].
 */
interface PerformanceMonitoring {
    companion object {
        // Custom trace names for key user flows
        const val TRACE_AGENDA_LOAD = "agenda_load"
        const val TRACE_SPEAKERS_LOAD = "speakers_load"
        const val TRACE_SESSION_DETAILS_LOAD = "session_details_load"
        const val TRACE_SPEAKER_DETAILS_LOAD = "speaker_details_load"

        // Custom attributes
        const val ATTR_DATA_SOURCE = "data_source"
        const val ATTR_ERROR_TYPE = "error_type"
    }

    /**
     * Runs [block] inside a custom trace named [traceName], adding [attributes] when it stops.
     */
    suspend fun <T> trace(
        traceName: String,
        attributes: Map<String, String> = emptyMap(),
        block: suspend () -> T
    ): T

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
    ): T
}
