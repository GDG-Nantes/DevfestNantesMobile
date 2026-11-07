import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Agenda port: its sessions, rooms and bookmark ids come from scripted
/// `FakeStream`s (through the real `KotlinStreamBridge`). Classification delegates to the real shared use
/// case unless a test scripts the two flags. The persisted filters are a plain snapshot, and every write is
/// recorded in order. The recorders are lock-free: the fake runs on the main actor in these tests.
final class FakeAgendaSource: AgendaSource {

    struct BookmarkWrite: Equatable {
        let sessionId: String
        let bookmarked: Bool
    }

    let sessionsStream: FakeStream<[Session]>
    let roomsStream: FakeStream<Set<Room>>
    let bookmarksStream: FakeStream<Set<String>>
    /// What `currentFilters()` returns.
    var filters: Set<shared.SessionFilter>
    private(set) var writtenFilters: [Set<shared.SessionFilter>] = []
    private(set) var writtenBookmarks: [BookmarkWrite] = []
    private let classifier = ClassifySessionUseCase()
    private let talkOverride: ((Session) -> Bool)?
    private let navigableOverride: ((Session) -> Bool)?

    init(
        sessionsStream: FakeStream<[Session]> = FakeStream(.hang),
        roomsStream: FakeStream<Set<Room>> = FakeStream(.hang),
        bookmarksStream: FakeStream<Set<String>> = FakeStream(.emit([[]], thenFinish: false)),
        filters: Set<shared.SessionFilter> = [],
        talkOverride: ((Session) -> Bool)? = nil,
        navigableOverride: ((Session) -> Bool)? = nil
    ) {
        self.sessionsStream = sessionsStream
        self.roomsStream = roomsStream
        self.bookmarksStream = bookmarksStream
        self.filters = filters
        self.talkOverride = talkOverride
        self.navigableOverride = navigableOverride
    }

    func sessions() -> AsyncThrowingStream<[Session], Error> {
        sessionsStream.stream()
    }

    func rooms() -> AsyncThrowingStream<Set<Room>, Error> {
        roomsStream.stream()
    }

    func isTalk(_ session: Session) -> Bool {
        talkOverride?(session) ?? classifier.isTalk(session: session)
    }

    func isNavigable(_ session: Session) -> Bool {
        navigableOverride?(session) ?? classifier.isNavigable(session: session)
    }

    func currentFilters() -> Set<shared.SessionFilter> {
        filters
    }

    func setFilters(_ filters: Set<shared.SessionFilter>) {
        writtenFilters.append(filters)
    }

    func bookmarkedIds() -> AsyncThrowingStream<Set<String>, Error> {
        bookmarksStream.stream()
    }

    func setBookmarked(_ sessionId: String, _ bookmarked: Bool) {
        writtenBookmarks.append(BookmarkWrite(sessionId: sessionId, bookmarked: bookmarked))
    }
}
