//
//  SpeakerDetailsSource.swift
//  DevFest Nantes
//

import shared
import KMPNativeCoroutinesAsync

/// What the SpeakerDetails screen reads: one speaker, the sessions they give and how those sessions are classified.
protocol SpeakerDetailsSource {
    func speaker(id: String) async throws -> Speaker?
    func speakerSessions(speakerId: String) async throws -> [Session]
    func isTalk(_ session: Session) -> Bool
    func isNavigable(_ session: Session) -> Bool
}

/// Live adapter over the shared store and the shared classification use case. Both are resolved lazily,
/// never in an initializer.
struct LiveSpeakerDetailsSource: SpeakerDetailsSource {
    private let store: () -> DevFestNantesStore
    private let classify: () -> ClassifySessionUseCase

    init(
        store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() },
        classify: @escaping () -> ClassifySessionUseCase = { IosDependencies.shared.classifySession() }
    ) {
        self.store = store
        self.classify = classify
    }

    func isTalk(_ session: Session) -> Bool {
        classify().isTalk(session: session)
    }

    func isNavigable(_ session: Session) -> Bool {
        classify().isNavigable(session: session)
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
