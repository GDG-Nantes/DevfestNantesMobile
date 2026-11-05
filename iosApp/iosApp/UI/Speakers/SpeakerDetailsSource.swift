//
//  SpeakerDetailsSource.swift
//  DevFest Nantes
//

import shared
import KMPNativeCoroutinesAsync

/// What the SpeakerDetails screen reads: one speaker and the sessions they give.
protocol SpeakerDetailsSource {
    func speaker(id: String) async throws -> Speaker?
    func speakerSessions(speakerId: String) async throws -> [Session]
}

/// Live adapter over the shared store. The store is resolved lazily, never in an initializer.
struct LiveSpeakerDetailsSource: SpeakerDetailsSource {
    private let store: () -> DevFestNantesStore

    init(store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() }) {
        self.store = store
    }

    func speaker(id: String) async throws -> Speaker? {
        let resolvedStore = store()
        return try await asyncFunction(for: resolvedStore.getSpeaker(id: id))
    }

    func speakerSessions(speakerId: String) async throws -> [Session] {
        let resolvedStore = store()
        return try await asyncFunction(for: resolvedStore.getSpeakerSessions(speakerId: speakerId))
    }
}
