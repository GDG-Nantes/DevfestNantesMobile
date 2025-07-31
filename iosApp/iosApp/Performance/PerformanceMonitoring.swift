import Foundation
import os
import Firebase
import FirebasePerformance

/**
 * iOS native Firebase Performance monitoring.
 * Uses Firebase Performance APIs directly (no shared module abstraction).
 */
public class PerformanceMonitoring: ObservableObject {
    
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "PerformanceMonitoring")
    
    // MARK: - Singleton
    
    static let shared = PerformanceMonitoring()
    
    // MARK: - Constants
    
    // Custom trace names for key user flows (matching Android implementation)
    static let TRACE_AGENDA_LOAD = "agenda_load"
    static let TRACE_SPEAKERS_LOAD = "speakers_load"
    static let TRACE_SESSION_DETAILS_LOAD = "session_details_load"
    static let TRACE_SPEAKER_DETAILS_LOAD = "speaker_details_load"
    
    // Custom attributes
    static let ATTR_DATA_SOURCE = "data_source"
    static let ATTR_ERROR_TYPE = "error_type"
    
    // MARK: - App Startup Tracking
    
    private static var appStartupTrace: Trace?
    
    /**
     * Starts tracking app startup performance using native Firebase Performance.
     */
    public static func startAppStartupTrace() {
        guard appStartupTrace == nil else { return }
        
        guard let trace = Performance.startTrace(name: "app_startup") else {
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "PerformanceMonitoring")
                .error("Failed to start app startup trace - Performance.startTrace returned nil")
            return
        }
        
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
     * @param traceName The name of the trace (use predefined constants for consistency)
     * @param dataSource The source of the data (e.g., "graphql", "cache")
     * @param operation The async operation to track
     */
    func trackDataLoad<T>(
        traceName: String,
        dataSource: String = "unknown",
        operation: @escaping () async throws -> T
    ) async throws -> T {
        guard let trace = Performance.startTrace(name: traceName) else {
            logger.error("Failed to start data load trace: \(traceName) - Performance.startTrace returned nil")
            // Still execute the operation even if tracing fails
            return try await operation()
        }
        
        let startTime = Date()
        
        trace.setValue(traceName, forAttribute: "operation")
        trace.setValue(dataSource, forAttribute: PerformanceMonitoring.ATTR_DATA_SOURCE)
        
        logger.info("Started data load trace: \(traceName)")
        
        do {
            let result = try await operation()
            let duration = Date().timeIntervalSince(startTime) * 1000
            
            trace.setValue(String(Int(duration)), forAttribute: "duration_ms")
            trace.setValue("true", forAttribute: "success")
            trace.stop()
            
            logger.info("Completed data load trace: \(traceName) in \(Int(duration))ms")
            return result
        } catch {
            let duration = Date().timeIntervalSince(startTime) * 1000
            
            trace.setValue(String(Int(duration)), forAttribute: "duration_ms")
            trace.setValue("false", forAttribute: "success")
            trace.setValue(String(describing: type(of: error)), forAttribute: PerformanceMonitoring.ATTR_ERROR_TYPE)
            trace.stop()
            
            logger.error("Failed data load trace: \(traceName) - \(error.localizedDescription)")
            throw error
        }
    }
    
    /**
     * Tracks SwiftUI navigation performance.
     */
    func trackNavigation<T>(
        to screen: String,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        guard let trace = Performance.startTrace(name: "screen_navigation_\(screen)") else {
            logger.error("Failed to start navigation trace to: \(screen) - Performance.startTrace returned nil")
            // Still execute the operation even if tracing fails
            return try await operation()
        }
        
        let startTime = Date()
        
        trace.setValue(screen, forAttribute: "screen_name")
        
        logger.info("Started navigation trace to: \(screen)")
        
        do {
            let result = try await operation()
            let duration = Date().timeIntervalSince(startTime) * 1000
            
            trace.setValue(String(Int(duration)), forAttribute: "duration_ms")
            trace.setValue("true", forAttribute: "success")
            trace.stop()
            
            logger.info("Completed navigation trace to: \(screen) in \(Int(duration))ms")
            return result
        } catch {
            let duration = Date().timeIntervalSince(startTime) * 1000
            
            trace.setValue(String(Int(duration)), forAttribute: "duration_ms")
            trace.setValue("false", forAttribute: "success")
            trace.setValue(String(describing: type(of: error)), forAttribute: PerformanceMonitoring.ATTR_ERROR_TYPE)
            trace.stop()
            
            logger.error("Failed navigation trace to: \(screen) - \(error.localizedDescription)")
            throw error
        }
    }
    
    // MARK: - Screen Performance Tracking
    
    /**
     * Tracks screen loading performance.
     * @return Optional Trace that should be passed to stopScreenLoadTrace, or nil if trace creation failed
     */
    func trackScreenLoad(screenName: String) -> Trace? {
        guard let trace = Performance.startTrace(name: "screen_load_\(screenName)") else {
            logger.error("Failed to start screen load trace for: \(screenName) - Performance.startTrace returned nil")
            return nil
        }
        
        trace.setValue(screenName, forAttribute: "screen_name")
        
        logger.info("Started screen load trace for: \(screenName)")
        return trace
    }
    
    /**
     * Stops a screen loading trace.
     */
    func stopScreenLoadTrace(_ trace: Trace?, success: Bool = true, itemCount: Int? = nil) {
        guard let trace = trace else {
            logger.warning("Cannot stop screen load trace - trace is nil")
            return
        }
        
        trace.setValue(String(success), forAttribute: "success")
        
        if let count = itemCount {
            trace.setValue(String(count), forAttribute: "item_count")
        }
        
        trace.stop()
        logger.info("Stopped screen load trace - success: \(success)")
    }
    
    // MARK: - Network Request Tracking
    
    /**
     * Tracks network request performance.
     * @return Optional Trace that should be passed to stopNetworkRequestTrace, or nil if trace creation failed
     */
    func trackNetworkRequest(url: String, httpMethod: String) -> Trace? {
        guard let trace = Performance.startTrace(name: "network_request") else {
            logger.error("Failed to start network request trace for: \(url) - Performance.startTrace returned nil")
            return nil
        }
        
        trace.setValue(url, forAttribute: "url")
        trace.setValue(httpMethod, forAttribute: "http_method")
        
        logger.info("Started network request trace for: \(url)")
        return trace
    }
    
    /**
     * Stops a network request trace with response details.
     */
    func stopNetworkRequestTrace(_ trace: Trace?, responseCode: Int, responseSize: Int64? = nil) {
        guard let trace = trace else {
            logger.warning("Cannot stop network request trace - trace is nil")
            return
        }
        
        trace.setValue(String(responseCode), forAttribute: "response_code")
        
        if let size = responseSize {
            trace.setValue(String(size), forAttribute: "response_size")
        }
        
        trace.stop()
        logger.info("Stopped network request trace with response code: \(responseCode)")
    }
    
    // MARK: - UI Rendering Tracking
    
    /**
     * Tracks UI rendering operations.
     * @return Optional Trace that should be passed to stopUIRenderingTrace, or nil if trace creation failed
     */
    func trackUIRendering(componentName: String) -> Trace? {
        guard let trace = Performance.startTrace(name: "ui_render_\(componentName)") else {
            logger.error("Failed to start UI rendering trace for: \(componentName) - Performance.startTrace returned nil")
            return nil
        }
        
        trace.setValue(componentName, forAttribute: "component")
        
        logger.info("Started UI rendering trace for: \(componentName)")
        return trace
    }
    
    /**
     * Stops a UI rendering trace.
     */
    func stopUIRenderingTrace(_ trace: Trace?) {
        guard let trace = trace else {
            logger.warning("Cannot stop UI rendering trace - trace is nil")
            return
        }
        
        trace.stop()
        logger.info("Stopped UI rendering trace")
    }
    
    // MARK: - General Purpose Async Tracking
    
    /**
     * Tracks any async operation with automatic trace management.
     */
    func trackAsyncOperation<T>(
        name: String,
        attributes: [String: String] = [:],
        operation: @escaping () async throws -> T
    ) async rethrows -> T {
        guard let trace = Performance.startTrace(name: name) else {
            logger.error("Failed to start async operation trace: \(name) - Performance.startTrace returned nil")
            // Still execute the operation even if tracing fails
            return try await operation()
        }
        
        let startTime = Date()
        
        // Set initial attributes
        for (key, value) in attributes {
            trace.setValue(value, forAttribute: key)
        }
        
        logger.info("Started async operation trace: \(name)")
        
        do {
            let result = try await operation()
            let duration = Date().timeIntervalSince(startTime) * 1000 // Convert to milliseconds
            
            trace.setValue(String(Int(duration)), forAttribute: "duration_ms")
            trace.setValue("true", forAttribute: "success")
            trace.stop()
            
            logger.info("Completed async operation trace: \(name) in \(Int(duration))ms")
            return result
        } catch {
            let duration = Date().timeIntervalSince(startTime) * 1000
            
            trace.setValue(String(Int(duration)), forAttribute: "duration_ms")
            trace.setValue("false", forAttribute: "success")
            trace.setValue(String(describing: type(of: error)), forAttribute: PerformanceMonitoring.ATTR_ERROR_TYPE)
            trace.stop()
            
            logger.error("Failed async operation trace: \(name) - \(error.localizedDescription)")
            throw error
        }
    }
}

// MARK: - Convenience Extensions

extension PerformanceMonitoring {
    
    /**
     * Convenience method for tracking data loading operations in ViewModels.
     * Similar to Android's traceDataLoading extension function.
     */
    func traceDataLoading<T>(
        operation: String,
        dataSource: String = "graphql",
        block: @escaping () async throws -> T
    ) async throws -> T {
        return try await trackDataLoad(
            traceName: operation,
            dataSource: dataSource,
            operation: block
        )
    }
    
    /**
     * Convenience method for tracking screen navigation.
     */
    func traceNavigation<T>(
        to screen: String,
        block: @escaping () async throws -> T
    ) async throws -> T {
        return try await trackNavigation(to: screen, operation: block)
    }
    
    /**
     * Convenience method for tracking any operation with custom attributes.
     */
    func trace<T>(
        name: String,
        attributes: [String: String] = [:],
        block: @escaping () async throws -> T
    ) async rethrows -> T {
        return try await trackAsyncOperation(name: name, attributes: attributes, operation: block)
    }
}
