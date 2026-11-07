import Testing
import Foundation
import os
import shared
@testable import DevFest_Nantes

/// Agenda ViewModel tests. Every test builds its own source fake, tracing fake and ViewModel, so nothing is
/// shared between tests. The Kotlin filter type is always written `shared.SessionFilter`: the app still
/// declares its own `SessionFilter` struct.
@MainActor @Suite(.timeLimit(.minutes(1))) struct AgendaViewModelTests {

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the ViewModel end to end
    /// through `KotlinStreamBridge` and the store's native flows (no network or Firebase), and the sessions
    /// load goes through the tracing seam under the v1.0 trace name and data source. The filter seed and the
    /// bookmark seed resolve the real Koin stores, so Koin is started first (idempotent); both stores only
    /// read here, nothing is written.
    @Test func live_source_loads_the_mocked_store_sessions_and_rooms() async {
        TestHost.start()
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        let tracing = RecordingDataLoadTracing()
        let viewModel = AgendaViewModel(source: LiveAgendaSource(store: { store }, classify: { ClassifySessionUseCase() }), tracing: tracing)

        await viewModel.observeSessions()
        await viewModel.observeRooms()

        #expect(!viewModel.agendaContent.sections.isEmpty)
        #expect(viewModel.isLoading == false)
        #expect(!(viewModel.roomsContent ?? []).isEmpty)
        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
    }

    // MARK: - Helpers

    private struct TestError: Error {}

    private func room(_ id: String) -> Room {
        Room(id: id, name: "Room \(id)", sortIndex: 0)
    }

    private func makeViewModel(
        source: FakeAgendaSource,
        tracing: RecordingDataLoadTracing = RecordingDataLoadTracing()
    ) -> AgendaViewModel {
        AgendaViewModel(source: source, tracing: tracing)
    }

    private func sectionSessionIds(_ viewModel: AgendaViewModel) -> [String] {
        viewModel.agendaContent.sections.flatMap { $0.sessions.map(\.id) }
    }

    // MARK: - Sessions

    @Test func loads_only_the_first_sessions_emission_and_records_the_agenda_trace() async throws {
        let all = try TestData.sessions()
        let listA = Array(all.prefix(3))
        let listB = Array(all.suffix(2))
        let stream = FakeStream<[Session]>(.emit([listA, listB], thenFinish: false))
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: stream), tracing: tracing)

        await viewModel.observeSessions()
        await stream.waitUntilTerminated()

        #expect(Set(sectionSessionIds(viewModel)) == Set(listA.map(\.id)))
        #expect(viewModel.isLoading == false)
        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
        #expect(stream.terminationCount == 1)
    }

    @Test func rows_take_their_talk_and_navigable_flags_from_the_source() async throws {
        let stream = FakeStream<[Session]>(.emit([try TestData.sessions()], thenFinish: false))
        let source = FakeAgendaSource(
            sessionsStream: stream,
            talkOverride: { _ in false }
        )
        let viewModel = makeViewModel(source: source)

        await viewModel.observeSessions()

        let rows = viewModel.agendaContent.sections.flatMap(\.sessions)
        #expect(!rows.isEmpty)
        #expect(rows.allSatisfy { !$0.isATalk })
        #expect(rows.allSatisfy { $0.isNavigable == ($0.sessionType == .conference || $0.sessionType == .codelab || $0.sessionType == .quickie) })
    }

    @Test func sessions_error_stops_loading_with_an_empty_agenda() async {
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: FakeStream(.fail(TestError()))))

        await viewModel.observeSessions()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    @Test func sessions_stream_without_emission_stops_loading_with_an_empty_agenda() async {
        let stream = FakeStream<[Session]>(.emit([], thenFinish: true))
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: stream))

        await viewModel.observeSessions()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    @Test func cancelled_sessions_load_keeps_the_spinner() async {
        let stream = FakeStream<[Session]>(.hang)
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: stream))

        let load = Task { @MainActor in await viewModel.observeSessions() }
        await stream.waitUntilSubscribed()
        load.cancel()
        await load.value
        await stream.waitUntilTerminated()

        #expect(viewModel.isLoading == true)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    // MARK: - Rooms

    @Test func rooms_follow_every_emission_until_the_stream_ends() async {
        let first: Set<Room> = [room("r1")]
        let second: Set<Room> = [room("r2"), room("r3")]
        let stream = FakeStream<Set<Room>>(.emit([first, second], thenFinish: true))
        let viewModel = makeViewModel(source: FakeAgendaSource(roomsStream: stream))

        await viewModel.observeRooms()

        #expect(Set((viewModel.roomsContent ?? []).map(\.id)) == ["r2", "r3"])
    }

    @Test func cancelled_rooms_collection_is_silent_and_keeps_the_rooms() async {
        let stream = FakeStream<Set<Room>>(.emit([[room("r1")]], thenFinish: false))
        let viewModel = makeViewModel(source: FakeAgendaSource(roomsStream: stream))

        let collection = Task { @MainActor in await viewModel.observeRooms() }
        await stream.waitUntilSubscribed()
        collection.cancel()
        await collection.value
        await stream.waitUntilTerminated()

        #expect(viewModel.roomsContent?.map(\.id) == ["r1"])
        #expect(stream.terminationCount == 1)
    }

    // MARK: - Bookmark and filter mirrors (write-through to the shared stores)

    private func filter(_ type: SessionFilterType, _ value: String) -> shared.SessionFilter {
        shared.SessionFilter(type: type, value: value)
    }

    @Test func init_seeds_the_filter_mirror_from_the_source_and_writes_nothing() {
        let seeded: Set<shared.SessionFilter> = [filter(.language, "FRENCH"), filter(.room, "room-1")]
        let source = FakeAgendaSource(filters: seeded)

        let viewModel = makeViewModel(source: source)

        #expect(viewModel.sessionFilters == seeded)
        #expect(viewModel.bookmarkedIds.isEmpty)
        #expect(source.writtenFilters.isEmpty)
        #expect(source.writtenBookmarks.isEmpty)
        #expect(source.bookmarksStream.subscriptionCount == 0)
    }

    @Test func toggle_bookmark_writes_through_and_notifies_observers() throws {
        let source = FakeAgendaSource()
        let viewModel = makeViewModel(source: source)
        let session = AgendaContent.Session(from: try #require(try TestData.sessions().first))

        let notified = OSAllocatedUnfairLock(initialState: false)
        withObservationTracking {
            _ = viewModel.bookmarkedIds
        } onChange: {
            notified.withLock { $0 = true }
        }
        viewModel.toggleBookmark(sessionId: session.id)

        #expect(notified.withLock { $0 })
        #expect(viewModel.bookmarkedIds == [session.id])
        #expect(source.writtenBookmarks == [.init(sessionId: session.id, bookmarked: true)])

        viewModel.toggleBookmark(sessionId: session.id)

        #expect(viewModel.bookmarkedIds.isEmpty)
        #expect(source.writtenBookmarks == [
            .init(sessionId: session.id, bookmarked: true),
            .init(sessionId: session.id, bookmarked: false)
        ])
    }

    @Test func toggle_filter_writes_through_with_one_element_per_value() {
        let existing = filter(.room, "room-1")
        let source = FakeAgendaSource(filters: [existing])
        let viewModel = makeViewModel(source: source)

        // A separate instance equal by value is the same element (07 D-14).
        #expect(Set([existing, filter(.room, "room-1")]).count == 1)

        viewModel.toggleFilter(type: .room, value: "room-1")

        #expect(viewModel.sessionFilters.isEmpty)

        viewModel.toggleFilter(type: .room, value: "room-1")
        viewModel.toggleFilter(type: .language, value: "FRENCH")

        #expect(viewModel.sessionFilters == [existing, filter(.language, "FRENCH")])
        #expect(source.writtenFilters == [
            [],
            [existing],
            [existing, filter(.language, "FRENCH")]
        ])
    }

    @Test func clear_filters_writes_an_empty_set_through_the_store() {
        let source = FakeAgendaSource(filters: [filter(.room, "room-1"), filter(.bookmark, "bookmark")])
        let viewModel = makeViewModel(source: source)

        viewModel.clearFilters()

        #expect(viewModel.sessionFilters.isEmpty)
        #expect(source.writtenFilters == [[]])
    }

    @Test func bookmarks_are_seeded_once_from_the_first_emission() async throws {
        let sessions = FakeStream<[Session]>(.emit([try TestData.sessions()], thenFinish: false))
        let bookmarks = FakeStream<Set<String>>(.emit([["a"], ["a", "stale"]], thenFinish: false))
        let source = FakeAgendaSource(sessionsStream: sessions, bookmarksStream: bookmarks)
        let viewModel = makeViewModel(source: source)

        await viewModel.observeSessions()

        #expect(viewModel.bookmarkedIds == ["a"])

        viewModel.toggleBookmark(sessionId: "b")
        await viewModel.observeSessions()

        #expect(bookmarks.subscriptionCount == 1)
        #expect(viewModel.bookmarkedIds == ["a", "b"])
        #expect(source.writtenBookmarks == [.init(sessionId: "b", bookmarked: true)])
    }

    @Test func empty_stores_start_with_no_bookmarks_and_no_filters() async throws {
        let sessions = FakeStream<[Session]>(.emit([try TestData.sessions()], thenFinish: false))
        let source = FakeAgendaSource(sessionsStream: sessions)
        let viewModel = makeViewModel(source: source)

        #expect(viewModel.sessionFilters.isEmpty)
        #expect(viewModel.bookmarkedIds.isEmpty)

        await viewModel.observeSessions()

        #expect(viewModel.isLoading == false)
        #expect(!viewModel.agendaContent.sections.isEmpty)
        #expect(source.bookmarksStream.subscriptionCount == 1)
        #expect(viewModel.bookmarkedIds.isEmpty)
        #expect(source.writtenBookmarks.isEmpty)
        #expect(source.writtenFilters.isEmpty)
    }

    // MARK: - Intents on the mirrors

    @Test func toggle_bookmark_returns_the_post_toggle_value() {
        let source = FakeAgendaSource()
        let viewModel = makeViewModel(source: source)

        #expect(viewModel.toggleBookmark(sessionId: "s1") == true)
        #expect(viewModel.isBookmarked("s1") == true)

        #expect(viewModel.toggleBookmark(sessionId: "s1") == false)
        #expect(viewModel.isBookmarked("s1") == false)
        #expect(source.writtenBookmarks.map(\.bookmarked) == [true, false])
    }

    @Test func toggling_a_filter_twice_restores_the_previous_set() {
        let source = FakeAgendaSource()
        let viewModel = makeViewModel(source: source)
        let other = filter(.language, "FRENCH")
        viewModel.toggleFilter(type: .language, value: "FRENCH")

        viewModel.toggleFilter(type: .room, value: "room-1")

        #expect(viewModel.sessionFilters == [other, filter(.room, "room-1")])

        viewModel.toggleFilter(type: .room, value: "room-1")

        #expect(viewModel.sessionFilters == [other])
        #expect(source.writtenFilters.last == [other])
        #expect(source.writtenFilters.count == 3)
    }
}
