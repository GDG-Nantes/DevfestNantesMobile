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
    private var completions = 0
    private let completedSignal: AsyncStream<Void>.Continuation
    private var completedIterator: AsyncStream<Void>.Iterator

    init() {
        let completed = AsyncStream<Void>.makeStream()
        completedSignal = completed.continuation
        completedIterator = completed.stream.makeAsyncIterator()
    }

    /// The traces started so far, in order.
    var recordedTraces: [RecordedTrace] {
        lock.withLock { traces }
    }

    /// The traces whose operation returned normally so far (a throwing operation is not counted).
    var completedTraceCount: Int {
        lock.withLock { completions }
    }

    /// Returns once one more trace completed (each call consumes one completion signal).
    func waitUntilCompleted() async {
        _ = await completedIterator.next()
    }

    func trackDataLoad<T>(
        traceName: String,
        dataSource: String,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        lock.withLock { traces.append(RecordedTrace(traceName: traceName, dataSource: dataSource)) }
        let result = try await operation()
        lock.withLock { completions += 1 }
        completedSignal.yield(())
        return result
    }
}
