import Foundation
import os
import FirebaseCrashlytics

enum LogLevel {
    case debug, info, warning, error
}

final class DevFestLogger {
    private let logger: Logger

    init(category: String) {
        self.logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: category)
    }

    func log(_ level: LogLevel, _ message: String, error: Error? = nil) {
        switch level {
        case .debug:
            logger.debug("\(message, privacy: .public)")
        case .info:
            logger.info("\(message, privacy: .public)")
        case .warning:
            logger.warning("\(message, privacy: .public)")
            Crashlytics.crashlytics().log("Warning: \(message)")
            if let error = error {
                Crashlytics.crashlytics().record(error: error)
            }
        case .error:
            logger.error("\(message, privacy: .public)")
            Crashlytics.crashlytics().log("Error: \(message)")
            if let error = error {
                Crashlytics.crashlytics().record(error: error)
            }
        }
    }
}
