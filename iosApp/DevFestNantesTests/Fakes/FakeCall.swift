import Foundation

/// Scripted stand-in for a one-shot Kotlin suspend call, shaped like the async function a live adapter awaits.
///
/// The n-th call plays the n-th script; the last script repeats. Synchronization uses a handshake
/// (`waitUntilCalled()`, no polling or timing waits); the counter is lock-guarded because the call can run
/// off the main actor.
final class FakeCall<Value>: @unchecked Sendable {

    enum Script {
        /// Returns the value.
        case value(Value)
        /// Throws the error.
        case fail(Error)
        /// Suspends until the awaiting task is cancelled, then throws `CancellationError`.
        case suspendUntilCancelled
    }

    private let scripts: [Script]
    private let lock = NSLock()
    private var calls = 0

    private let calledSignal: AsyncStream<Void>.Continuation
    private var calledIterator: AsyncStream<Void>.Iterator

    init(_ scripts: Script...) {
        precondition(!scripts.isEmpty, "FakeCall needs at least one script")
        self.scripts = scripts
        let called = AsyncStream<Void>.makeStream()
        calledSignal = called.continuation
        calledIterator = called.stream.makeAsyncIterator()
    }

    /// Number of times the call was made.
    var callCount: Int {
        lock.lock()
        defer { lock.unlock() }
        return calls
    }

    /// Returns once one more call was made (each wait consumes one call signal).
    func waitUntilCalled() async {
        _ = await calledIterator.next()
    }

    /// The scripted call: counts and signals first, then plays the script.
    func call() async throws -> Value {
        let index = lock.withLock {
            let current = calls
            calls += 1
            return current
        }

        calledSignal.yield(())

        switch scripts[min(index, scripts.count - 1)] {
        case .value(let value):
            return value
        case .fail(let error):
            throw error
        case .suspendUntilCancelled:
            // Cancellation-driven, not a timing wait: the sleep throws `CancellationError` as soon as the task is cancelled.
            while true {
                try await Task.sleep(for: .seconds(3600))
            }
        }
    }
}
