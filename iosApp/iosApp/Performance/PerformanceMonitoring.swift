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
        dataSource: String = "unknown",
        operation: @escaping () async throws -> T
    ) async throws -> T {
        let trace = Performance.startTrace(name: "data_load_\(traceName)")
        let startTime = Date()
        
        trace.setValue(traceName, forAttribute: "operation")
        trace.setValue(dataSource, forAttribute: "data_source")
        
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
            trace.setValue(String(describing: type(of: error)), forAttribute: "error_type")
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
        let trace = Performance.startTrace(name: "screen_navigation_\(screen)")
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
            trace.setValue(String(describing: type(of: error)), forAttribute: "error_type")
            trace.stop()
            
            logger.error("Failed navigation trace to: \(screen) - \(error.localizedDescription)")
            throw error
        }
    }
    
    // MARK: - Screen Performance Tracking
    
    /**
     * Tracks screen loading performance.
     */
    func trackScreenLoad(screenName: String) -> Trace {
        let trace = Performance.startTrace(name: "screen_load_\(screenName)")
        trace.setValue(screenName, forAttribute: "screen_name")
        
        logger.info("Started screen load trace for: \(screenName)")
        return trace
    }
    
    /**
     * Stops a screen loading trace.
     */
    func stopScreenLoadTrace(_ trace: Trace, success: Bool = true, itemCount: Int? = nil) {
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
     */
    func trackNetworkRequest(url: String, httpMethod: String) -> Trace {
        let trace = Performance.startTrace(name: "network_request")
        
        trace.setValue(url, forAttribute: "url")
        trace.setValue(httpMethod, forAttribute: "http_method")
        
        logger.info("Started network request trace for: \(url)")
        return trace
    }
    
    /**
     * Stops a network request trace with response details.
     */
    func stopNetworkRequestTrace(_ trace: Trace, responseCode: Int, responseSize: Int64? = nil) {
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
     */
    func trackUIRendering(componentName: String) -> Trace {
        let trace = Performance.startTrace(name: "ui_render_\(componentName)")
        trace.setValue(componentName, forAttribute: "component")
        
        logger.info("Started UI rendering trace for: \(componentName)")
        return trace
    }
    
    /**
     * Stops a UI rendering trace.
     */
    func stopUIRenderingTrace(_ trace: Trace) {
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
        let trace = Performance.startTrace(name: name)
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
            trace.setValue(String(describing: type(of: error)), forAttribute: "error_type")
            trace.stop()
            
            logger.error("Failed async operation trace: \(name) - \(error.localizedDescription)")
            throw error
        }
    }
}
