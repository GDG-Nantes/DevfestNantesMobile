//
//  DataLoadTracing.swift
//  DevFest Nantes
//

/// Seam over the data-load performance traces, so ViewModels can be tested without Firebase Performance.
/// The signature matches `PerformanceMonitoring.trackDataLoad`, which conforms without extra code.
protocol DataLoadTracing {
    func trackDataLoad<T>(
        traceName: String,
        dataSource: String,
        operation: @escaping () async throws -> T
    ) async throws -> T
}

extension PerformanceMonitoring: DataLoadTracing {}
