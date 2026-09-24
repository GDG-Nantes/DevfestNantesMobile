package com.gdgnantes.devfest.androidapp.core.performance

/**
 * Extension function to trace ViewModel data loading operations.
 */
suspend inline fun <T> PerformanceMonitoring.traceDataLoading(
    operation: String,
    dataSource: String = "unknown",
    noinline block: suspend () -> T
): T {
    return trackDataLoad(
        traceName = operation,
        dataSource = dataSource,
        block = block
    )
}

/**
 * Extension function to trace UI state updates.
 */
suspend inline fun <T> PerformanceMonitoring.traceStateUpdate(
    screenName: String,
    noinline block: suspend () -> T
): T {
    return trace(
        traceName = "state_update_$screenName",
        attributes = mapOf("screen" to screenName),
        block = block
    )
}

/**
 * Extension function to trace network requests manually.
 */
suspend inline fun <T> PerformanceMonitoring.traceNetworkRequest(
    endpoint: String,
    method: String = "GET",
    noinline block: suspend () -> T
): T {
    return trace(
        traceName = "network_request",
        attributes =
            mapOf(
            "endpoint" to endpoint,
            "method" to method
        ),
        block = block
    )
}

/**
 * Extension function to trace compose recomposition performance.
 */
suspend inline fun <T> PerformanceMonitoring.traceComposition(
    componentName: String,
    noinline block: suspend () -> T
): T {
    return trace(
        traceName = "compose_recomposition",
        attributes = mapOf("component" to componentName),
        block = block
    )
}
