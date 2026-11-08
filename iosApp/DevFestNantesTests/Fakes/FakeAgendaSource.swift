import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Agenda port: its day schedules, rooms and bookmark ids come from scripted
/// `FakeStream`s (through the real `KotlinStreamBridge`). Classification delegates to the real shared use
/// case unless a test scripts the two flags. The persisted filters are a plain snapshot, and every write is
/// recorded in order. The persisted bookmarks are also a snapshot (`storedBookmarks`, updated by every write,
/// like the synchronous Kotlin store), and each per-session read is recorded in `bookmarkReads`. The recorders are lock-free: the fake runs on the main actor in these tests.
final class FakeAgendaSource: AgendaSource {

    struct BookmarkWrite: Equatable {
        let sessionId: String
        let bookmarked: Bool
    }

    let agendaStream: FakeStream<[DaySchedule]>
    let roomsStream: FakeStream<Set<Room>>
    let bookmarksStream: FakeStream<Set<String>>
    /// What `isBookmarked(_:)` answers from; models the Kotlin store's current value.
    private(set) var storedBookmarks: Set<String>
    /// What `currentFilters()` returns.
    var filters: Set<SessionFilter>
    private(set) var writtenFilters: [Set<SessionFilter>] = []
    private(set) var writtenBookmarks: [BookmarkWrite] = []
    private(set) var bookmarkReads: [String] = []
    private let classifier = ClassifySessionUseCase()
    private let talkOverride: ((Session) -> Bool)?
    private let navigableOverride: ((Session) -> Bool)?

    init(
        agendaStream: FakeStream<[DaySchedule]> = FakeStream(.hang),
        roomsStream: FakeStream<Set<Room>> = FakeStream(.hang),
        bookmarksStream: FakeStream<Set<String>> = FakeStream(.emit([[]], thenFinish: false)),
        storedBookmarks: Set<String> = [],
        filters: Set<SessionFilter> = [],
        talkOverride: ((Session) -> Bool)? = nil,
        navigableOverride: ((Session) -> Bool)? = nil
    ) {
        self.agendaStream = agendaStream
        self.roomsStream = roomsStream
        self.bookmarksStream = bookmarksStream
        self.storedBookmarks = storedBookmarks
        self.filters = filters
        self.talkOverride = talkOverride
        self.navigableOverride = navigableOverride
    }

    func agenda() -> AsyncThrowingStream<[DaySchedule], Error> {
        agendaStream.stream()
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

    func currentFilters() -> Set<SessionFilter> {
        filters
    }

    func setFilters(_ filters: Set<SessionFilter>) {
        writtenFilters.append(filters)
    }

    func bookmarkedIds() -> AsyncThrowingStream<Set<String>, Error> {
        bookmarksStream.stream()
    }

    func isBookmarked(_ sessionId: String) -> Bool {
        bookmarkReads.append(sessionId)
        return storedBookmarks.contains(sessionId)
    }

    func setBookmarked(_ sessionId: String, _ bookmarked: Bool) {
        writtenBookmarks.append(BookmarkWrite(sessionId: sessionId, bookmarked: bookmarked))
        if bookmarked {
            storedBookmarks.insert(sessionId)
        } else {
            storedBookmarks.remove(sessionId)
        }
    }
}
