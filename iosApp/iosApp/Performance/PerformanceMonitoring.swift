import Foundation
import os
import FirebasePerformance

/**
 * iOS native Firebase Performance monitoring.
 * Uses Firebase Performance APIs directly (no shared module abstraction).
 */
public class PerformanceMonitoring: ObservableObject {
    
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "PerformanceMonitoring")
    
    // MARK: - Singleton
    
    static let shared = PerformanceMonitoring()
    
    // MARK: - App Startup Tracking
    
    private static var appStartupTrace: Trace?
    
    /**
     * Starts tracking app startup performance using native Firebase Performance.
     */
    public static func startAppStartupTrace() {
        guard appStartupTrace == nil else { return }
        
        let trace = Performance.startTrace(name: "app_startup")
        appStartupTrace = trace
        
        Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "PerformanceMonitoring")
            .info("Started native iOS app startup trace")
    }
    
    /**
     * Stops the app startup trace using native Firebase Performance.
     */
    public static func stopAppStartupTrace() {
        guard let trace = appStartupTrace else { return }
        
        trace.stop()
        
        Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "PerformanceMonitoring")
            .info("Stopped native iOS app startup trace")
        appStartupTrace = nil
    }
    
    // MARK: - Native iOS Performance Tracking
    
    /**
     * Tracks data loading performance using native Firebase Performance.
     */
    func trackDataLoad<T>(
        traceName: String,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        logger.info("Tracking iOS data load: \(traceName)")
        
        // TODO: Replace with native Firebase Performance when ready
        // let trace = Performance.startTrace(name: traceName)
        // defer { trace.stop() }
        
        return try await operation()
    }
    
    /**
     * Tracks SwiftUI navigation performance.
     */
    func trackNavigation<T>(
        to screen: String,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        return try await trackDataLoad(traceName: "navigation_\(screen)", operation: operation)
    }
}
