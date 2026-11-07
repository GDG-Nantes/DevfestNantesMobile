//
//  AgendaSource.swift
//  DevFest Nantes
//

import shared

/// What the Agenda screen reads and writes: the sessions, the rooms, the persisted bookmarks and filters.
/// The Kotlin filter type is always written `shared.SessionFilter`: the app still declares its own
/// `SessionFilter` struct.
protocol AgendaSource {
    func agenda() -> AsyncThrowingStream<[DaySchedule], Error>
    func rooms() -> AsyncThrowingStream<Set<Room>, Error>
    func isTalk(_ session: Session) -> Bool
    func isNavigable(_ session: Session) -> Bool
    /// The persisted filters right now (a snapshot of the shared store).
    func currentFilters() -> Set<shared.SessionFilter>
    /// Writes the full filter set through to the shared store.
    func setFilters(_ filters: Set<shared.SessionFilter>)
    /// The persisted bookmarked session ids, as a flow of the whole set.
    func bookmarkedIds() -> AsyncThrowingStream<Set<String>, Error>
    /// Writes one bookmark through to the shared store.
    func setBookmarked(_ sessionId: String, _ bookmarked: Bool)
}

/// Live adapter over the shared store, the shared classification use case and the shared bookmark and
/// filter stores. All are resolved lazily, never in an initializer.
struct LiveAgendaSource: AgendaSource {
    private let store: () -> DevFestNantesStore
    private let classify: () -> ClassifySessionUseCase
    private let bookmarksStore: () -> BookmarksStore
    private let filtersStore: () -> SessionFiltersStore

    init(
        store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() },
        classify: @escaping () -> ClassifySessionUseCase = { IosDependencies.shared.classifySession() },
        bookmarksStore: @escaping () -> BookmarksStore = { IosDependencies.shared.bookmarksStore() },
        filtersStore: @escaping () -> SessionFiltersStore = { IosDependencies.shared.sessionFiltersStore() }
    ) {
        self.store = store
        self.classify = classify
        self.bookmarksStore = bookmarksStore
        self.filtersStore = filtersStore
    }

    func agenda() -> AsyncThrowingStream<[DaySchedule], Error> {
        AsyncThrowingStream { $0.finish() }
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

    func currentFilters() -> Set<shared.SessionFilter> {
        filtersStore().filters
    }

    func setFilters(_ filters: Set<shared.SessionFilter>) {
        filtersStore().setFilters(filters: filters)
    }

    func bookmarkedIds() -> AsyncThrowingStream<Set<String>, Error> {
        let resolvedStore = bookmarksStore()
        return KotlinStreamBridge.stream(resolvedStore.bookmarkedSessionIds)
    }

    func setBookmarked(_ sessionId: String, _ bookmarked: Bool) {
        bookmarksStore().setBookmarked(sessionId: sessionId, bookmarked: bookmarked)
    }
}
