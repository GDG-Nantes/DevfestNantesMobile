import Foundation
@testable import DevFest_Nantes

/// Hand-written fake of the tracing seam: records each trace and runs the operation, never touching Firebase.
final class RecordingDataLoadTracing: DataLoadTracing, @unchecked Sendable {

    struct RecordedTrace: Equatable {
        let traceName: String
        let dataSource: String
    }

    private let lock = NSLock()
    private var traces: [RecordedTrace] = []

    /// The traces started so far, in order.
    var recordedTraces: [RecordedTrace] {
        lock.withLock { traces }
    }

    func trackDataLoad<T>(
        traceName: String,
        dataSource: String,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        lock.withLock { traces.append(RecordedTrace(traceName: traceName, dataSource: dataSource)) }
        return try await operation()
    }
}
