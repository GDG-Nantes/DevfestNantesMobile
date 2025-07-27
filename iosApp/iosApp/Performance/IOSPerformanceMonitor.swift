import Foundation
import os
// TODO: Add back when ready to implement Firebase Performance
// import FirebaseCore
// import FirebasePerformance

/**
 * iOS native Firebase Performance implementation.
 * Uses Firebase Performance APIs directly without shared module abstraction.
 */
class IOSPerformanceMonitor: NSObject {
    
    private let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "IOSPerformanceMonitor")
    
    // TODO: Change to true when Firebase Performance is implemented
    var isAvailable: Bool {
        return false // Will be true when Firebase Performance is properly implemented
    }
    
    /**
     * Starts a custom trace using native Firebase Performance.
     */
    func startTrace(name: String, attributes: [String: String] = [:]) -> Any? {
        // TODO: Implement when Firebase Performance is added
        /*
        guard isAvailable else { return nil }
        
        let trace = Performance.startTrace(name: name)
        
        // Add attributes to trace
        for (key, value) in attributes {
            trace.setValue(value, forAttribute: key)
        }
        
        logger.info("Started Firebase Performance trace: \(name)")
        return trace
        */
        
        logger.info("Started placeholder trace: \(name) with attributes: \(attributes)")
        return name // Return string as placeholder
    }
    
    /**
     * Stops a trace using native Firebase Performance.
     */
    func stopTrace(_ trace: Any?, attributes: [String: String] = [:]) {
        // TODO: Implement when Firebase Performance is added
        /*
        guard let firebaseTrace = trace as? Trace else { return }
        
        // Add any additional attributes before stopping
        for (key, value) in attributes {
            firebaseTrace.setValue(value, forAttribute: key)
        }
        
        firebaseTrace.stop()
        logger.info("Stopped Firebase Performance trace")
        */
        
        if let traceName = trace as? String {
            logger.info("Stopped placeholder trace: \(traceName) with attributes: \(attributes)")
        }
    }
    
    /**
     * Records a metric for a trace using native Firebase Performance.
     */
    func recordMetric(for trace: Any?, metricName: String, value: Int64) {
        // TODO: Implement when Firebase Performance is added
        /*
        guard let firebaseTrace = trace as? Trace else { return }
        
        firebaseTrace.setIntValue(value, forMetric: metricName)
        logger.debug("Recorded metric \(metricName): \(value)")
        */
        
        logger.debug("Recorded placeholder metric \(metricName): \(value)")
    }
}


