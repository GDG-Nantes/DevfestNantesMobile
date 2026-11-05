//
//  AgendaSource.swift
//  DevFest Nantes
//

import shared

/// What the Agenda screen reads: the sessions and the rooms.
protocol AgendaSource {
    func sessions() -> AsyncThrowingStream<[Session], Error>
    func rooms() -> AsyncThrowingStream<Set<Room>, Error>
}

/// Live adapter over the shared store. The store is resolved lazily, never in an initializer.
struct LiveAgendaSource: AgendaSource {
    private let store: () -> DevFestNantesStore

    init(store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() }) {
        self.store = store
    }

    func sessions() -> AsyncThrowingStream<[Session], Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.sessions)
    }

    func rooms() -> AsyncThrowingStream<Set<Room>, Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.rooms)
    }
}
