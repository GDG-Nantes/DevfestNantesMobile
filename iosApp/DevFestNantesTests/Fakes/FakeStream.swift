import Foundation
@testable import DevFest_Nantes

/// Scripted stand-in for a Kotlin store flow, served through the real `KotlinStreamBridge`.
///
/// It has the shape of a KMP-NativeCoroutines native flow (item, completion and cancellation callbacks
/// returning a native cancellable, `Unit = Void`), so `stream()` exercises the bridge exactly like a live
/// adapter does. The n-th subscription plays the n-th script; the last script repeats.
/// Synchronization uses handshakes (no sleeps, no polling); counters are lock-guarded because the native
/// cancellable can run off the main actor.
final class FakeStream<Element>: @unchecked Sendable {

    enum Script {
        /// Delivers the values one by one (the next value only after the previous `next` callback ran),
        /// then completes normally when `thenFinish` is true, otherwise stays open.
        case emit([Element], thenFinish: Bool)
        /// Completes with an error without any value.
        case fail(Error)
        /// Never calls back and keeps the subscription open.
        case hang
    }

    private let scripts: [Script]
    private let lock = NSLock()
    private var subscriptions = 0
    private var terminations = 0

    private let subscribedSignal: AsyncStream<Void>.Continuation
    private let terminatedSignal: AsyncStream<Void>.Continuation
    private var subscribedIterator: AsyncStream<Void>.Iterator
    private var terminatedIterator: AsyncStream<Void>.Iterator

    init(_ scripts: Script...) {
        precondition(!scripts.isEmpty, "FakeStream needs at least one script")
        self.scripts = scripts
        let subscribed = AsyncStream<Void>.makeStream()
        let terminated = AsyncStream<Void>.makeStream()
        subscribedSignal = subscribed.continuation
        terminatedSignal = terminated.continuation
        subscribedIterator = subscribed.stream.makeAsyncIterator()
        terminatedIterator = terminated.stream.makeAsyncIterator()
    }

    /// Number of times the flow was subscribed.
    var subscriptionCount: Int {
        lock.lock()
        defer { lock.unlock() }
        return subscriptions
    }

    /// Number of times the native cancellable was invoked, i.e. native subscriptions the bridge cancelled.
    var terminationCount: Int {
        lock.lock()
        defer { lock.unlock() }
        return terminations
    }

    /// The scripted flow behind the real bridge.
    func stream() -> AsyncThrowingStream<Element, Error> {
        KotlinStreamBridge.stream(subscribe)
    }

    /// Returns once one more subscription happened (each call consumes one subscription signal).
    func waitUntilSubscribed() async {
        _ = await subscribedIterator.next()
    }

    /// Returns once one more native cancellation happened (each call consumes one termination signal).
    func waitUntilTerminated() async {
        _ = await terminatedIterator.next()
    }

    private func subscribe(
        _ onItem: @escaping (Element, @escaping () -> Void, Void) -> Void,
        _ onComplete: @escaping (Error?, Void) -> Void,
        _ onCancelled: @escaping (Error, Void) -> Void
    ) -> () -> Void {
        lock.lock()
        let index = subscriptions
        subscriptions += 1
        lock.unlock()

        subscribedSignal.yield(())

        switch scripts[min(index, scripts.count - 1)] {
        case .emit(let values, let thenFinish):
            deliver(values[...], thenFinish: thenFinish, onItem: onItem, onComplete: onComplete)
        case .fail(let error):
            onComplete(error, ())
        case .hang:
            break
        }

        return { [self] in
            lock.lock()
            terminations += 1
            lock.unlock()
            terminatedSignal.yield(())
        }
    }

    private func deliver(
        _ values: ArraySlice<Element>,
        thenFinish: Bool,
        onItem: @escaping (Element, @escaping () -> Void, Void) -> Void,
        onComplete: @escaping (Error?, Void) -> Void
    ) {
        guard let first = values.first else {
            if thenFinish { onComplete(nil, ()) }
            return
        }
        let rest = values.dropFirst()
        onItem(first, { [self] in
            deliver(rest, thenFinish: thenFinish, onItem: onItem, onComplete: onComplete)
        }, ())
    }
}
