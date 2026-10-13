package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.analytics.performance.PerformanceMonitoring

/** [PerformanceMonitoring] that runs the block untraced. */
class NoOpPerformanceMonitoring : PerformanceMonitoring {
    override suspend fun <T> trace(
        traceName: String,
        attributes: Map<String, String>,
        block: suspend () -> T
    ): T = block()

    override suspend fun <T> trackDataLoad(
        traceName: String,
        dataSource: String,
        itemCount: Int?,
        block: suspend () -> T
    ): T = block()
}
