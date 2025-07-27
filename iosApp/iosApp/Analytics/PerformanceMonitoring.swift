import Foundation
import os
import Firebase

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
            .info("Started app startup trace")
    }
    
    /**
     * Stops the app startup trace and records the measurement.
     */
    public static func stopAppStartupTrace() {
        guard let trace = appStartupTrace else { return }
        
        trace.stop()
        appStartupTrace = nil
        
        Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "PerformanceMonitoring")
            .info("Stopped app startup trace")
    }
    
    // MARK: - Screen Loading Tracking
    
    /**
     * Starts tracking screen loading performance.
     */
    public func trackScreenLoad(screenName: String) -> Trace {
        let trace = Performance.startTrace(name: "screen_load_\(screenName)")
        
        trace.setValue("screen_name", forAttribute: screenName)
        
        logger.info("Started screen load trace for: \(screenName)")
        return trace
    }
    
    /**
     * Stops a screen loading trace.
     */
    public func stopScreenLoadTrace(_ trace: Trace) {
        trace.stop()
        logger.info("Stopped screen load trace")
    }
    
    // MARK: - Network Request Tracking
    
    /**
     * Tracks network request performance.
     */
    public func trackNetworkRequest(url: String, httpMethod: String) -> Trace {
        let trace = Performance.startTrace(name: "network_request")
        
        trace.setValue("url", forAttribute: url)
        trace.setValue("http_method", forAttribute: httpMethod)
        
        logger.info("Started network request trace for: \(url)")
        return trace
    }
    
    /**
     * Stops a network request trace with response details.
     */
    public func stopNetworkRequestTrace(_ trace: Trace, responseCode: Int, responseSize: Int64? = nil) {
        trace.setValue("response_code", forAttribute: String(responseCode))
        
        if let size = responseSize {
            trace.setValue("response_size", forAttribute: String(size))
        }
        
        trace.stop()
        logger.info("Stopped network request trace with response code: \(responseCode)")
    }
}
