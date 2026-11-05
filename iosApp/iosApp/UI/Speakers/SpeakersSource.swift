//
//  SpeakersSource.swift
//  DevFest Nantes
//

import shared

/// What the Speakers screen reads: the list of speakers.
protocol SpeakersSource {
    func speakers() -> AsyncThrowingStream<[Speaker], Error>
}

/// Live adapter over the shared store. The store is resolved lazily, never in an initializer.
struct LiveSpeakersSource: SpeakersSource {
    private let store: () -> DevFestNantesStore

    init(store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() }) {
        self.store = store
    }

    func speakers() -> AsyncThrowingStream<[Speaker], Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.speakers)
    }
}
