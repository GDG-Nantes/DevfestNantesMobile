//
//  AgendaSource.swift
//  DevFest Nantes
//

import shared

/// What the Agenda screen reads: the sessions and the rooms.
protocol AgendaSource {
    func sessions() -> AsyncThrowingStream<[Session], Error>
    func rooms() -> AsyncThrowingStream<Set<Room>, Error>
    func isTalk(_ session: Session) -> Bool
    func isNavigable(_ session: Session) -> Bool
}

/// Live adapter over the shared store and the shared classification use case. Both are resolved lazily,
/// never in an initializer.
struct LiveAgendaSource: AgendaSource {
    private let store: () -> DevFestNantesStore
    private let classify: () -> ClassifySessionUseCase

    init(
        store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() },
        classify: @escaping () -> ClassifySessionUseCase = { IosDependencies.shared.classifySession() }
    ) {
        self.store = store
        self.classify = classify
    }

    func sessions() -> AsyncThrowingStream<[Session], Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.sessions)
    }

    func rooms() -> AsyncThrowingStream<Set<Room>, Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.rooms)
    }

    func isTalk(_ session: Session) -> Bool {
        classify().isTalk(session: session)
    }

    func isNavigable(_ session: Session) -> Bool {
        classify().isNavigable(session: session)
    }
}
