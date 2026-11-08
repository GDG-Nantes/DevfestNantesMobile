//
//  KotlinStreamBridge.swift
//  DevFest Nantes
//

import Foundation

/// Bridges a Kotlin flow exposed by KMP-NativeCoroutines to an `AsyncThrowingStream`.
///
/// Live adapters (`Live...Source`) pass a store flow property to `stream(_:)`, for example
/// `KotlinStreamBridge.stream(store.partners)`; ViewModels never call it. The parameter spells out the
/// KMP-NativeCoroutines `NativeFlow` shape (item, completion and cancellation callbacks returning a native
/// cancellable), so the bridge subscribes through those callbacks directly, with no task of its own, and
/// needs no KMP-NativeCoroutines import.
///
/// Termination (the consumer breaks, is cancelled or releases the stream) invokes the native cancellable,
/// which cancels the Kotlin collection.
///
/// Trade-off: the next native item is requested right after each yield (no pull-based backpressure), which
/// is fine for the small flows the app consumes: mostly single-emission ones, plus the continuous agenda
/// flow, which emits again when the data, the bookmarks or the filters change and is consumed as fast as it
/// emits.
enum KotlinStreamBridge {

    static func stream<Output, Failure: Error, Unit>(
        _ nativeFlow: @escaping (
            _ onItem: @escaping (Output, @escaping () -> Unit, Unit) -> Unit,
            _ onComplete: @escaping (Failure?, Unit) -> Unit,
            _ onCancelled: @escaping (Failure, Unit) -> Unit
        ) -> () -> Unit
    ) -> AsyncThrowingStream<Output, Error> {
        AsyncThrowingStream { continuation in
            let cancelNative = nativeFlow(
                { item, next, _ in
                    continuation.yield(item)
                    // Resumes the Kotlin collector; without it a continuous flow stalls after its first value.
                    return next()
                },
                { error, unit in
                    if let error {
                        continuation.finish(throwing: error)
                    } else {
                        continuation.finish()
                    }
                    return unit
                },
                { _, unit in
                    // A Kotlin-side cancellation stays silent for the consumer.
                    continuation.finish(throwing: CancellationError())
                    return unit
                }
            )
            continuation.onTermination = { _ in
                _ = cancelNative()
            }
        }
    }
}
