import Testing
import Foundation
import os
import shared
@testable import DevFest_Nantes

/// Agenda ViewModel tests. Every test builds its own source fake, tracing fake, throwaway defaults suite
/// and ViewModel, so nothing is shared between tests and the app's standard defaults are never touched.
@MainActor @Suite(.timeLimit(.minutes(1))) struct AgendaViewModelTests {

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the ViewModel end to end
    /// through `KotlinStreamBridge` and the store's native flows (no network, Firebase or Koin), and the
    /// sessions load goes through the tracing seam under the v1.0 trace name and data source.
    @Test func live_source_loads_the_mocked_store_sessions_and_rooms() async {
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        let tracing = RecordingDataLoadTracing()
        let suiteName = "AgendaViewModelTests-\(UUID().uuidString)"
        let suite = UserDefaults(suiteName: suiteName)!
        defer { suite.removePersistentDomain(forName: suiteName) }
        let viewModel = AgendaViewModel(source: LiveAgendaSource(store: { store }, classify: { ClassifySessionUseCase() }), tracing: tracing, defaults: suite)

        await viewModel.observeSessions()
        await viewModel.observeRooms()

        #expect(!viewModel.agendaContent.sections.isEmpty)
        #expect(viewModel.isLoading == false)
        #expect(!(viewModel.roomsContent ?? []).isEmpty)
        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
    }

    // MARK: - Helpers

    private struct TestError: Error {}

    /// Throwaway defaults suite with a unique name; `cleanup` removes its domain.
    private struct Suite {
        let name = "AgendaViewModelTests-\(UUID().uuidString)"
        let defaults: UserDefaults

        init() {
            defaults = UserDefaults(suiteName: name)!
        }

        func cleanup() {
            defaults.removePersistentDomain(forName: name)
        }
    }

    private func room(_ id: String) -> Room {
        Room(id: id, name: "Room \(id)", sortIndex: 0)
    }

    private func makeViewModel(
        source: FakeAgendaSource,
        suite: Suite,
        tracing: RecordingDataLoadTracing = RecordingDataLoadTracing()
    ) -> AgendaViewModel {
        AgendaViewModel(source: source, tracing: tracing, defaults: suite.defaults)
    }

    private func sectionSessionIds(_ viewModel: AgendaViewModel) -> [String] {
        viewModel.agendaContent.sections.flatMap { $0.sessions.map(\.id) }
    }

    // MARK: - Sessions

    @Test func loads_only_the_first_sessions_emission_and_records_the_agenda_trace() async throws {
        let suite = Suite()
        defer { suite.cleanup() }
        let all = try TestData.sessions()
        let listA = Array(all.prefix(3))
        let listB = Array(all.suffix(2))
        let stream = FakeStream<[Session]>(.emit([listA, listB], thenFinish: false))
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: stream), suite: suite, tracing: tracing)

        await viewModel.observeSessions()
        await stream.waitUntilTerminated()

        #expect(Set(sectionSessionIds(viewModel)) == Set(listA.map(\.id)))
        #expect(viewModel.isLoading == false)
        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
        #expect(stream.terminationCount == 1)
    }

    @Test func rows_take_their_talk_and_navigable_flags_from_the_source() async throws {
        let suite = Suite()
        defer { suite.cleanup() }
        let stream = FakeStream<[Session]>(.emit([try TestData.sessions()], thenFinish: false))
        let source = FakeAgendaSource(
            sessionsStream: stream,
            talkOverride: { _ in false }
        )
        let viewModel = makeViewModel(source: source, suite: suite)

        await viewModel.observeSessions()

        let rows = viewModel.agendaContent.sections.flatMap(\.sessions)
        #expect(!rows.isEmpty)
        #expect(rows.allSatisfy { !$0.isATalk })
        #expect(rows.allSatisfy { $0.isNavigable == ($0.sessionType == .conference || $0.sessionType == .codelab || $0.sessionType == .quickie) })
    }

    @Test func sessions_error_stops_loading_with_an_empty_agenda() async {
        let suite = Suite()
        defer { suite.cleanup() }
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: FakeStream(.fail(TestError()))), suite: suite)

        await viewModel.observeSessions()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    @Test func sessions_stream_without_emission_stops_loading_with_an_empty_agenda() async {
        let suite = Suite()
        defer { suite.cleanup() }
        let stream = FakeStream<[Session]>(.emit([], thenFinish: true))
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: stream), suite: suite)

        await viewModel.observeSessions()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    @Test func cancelled_sessions_load_keeps_the_spinner() async {
        let suite = Suite()
        defer { suite.cleanup() }
        let stream = FakeStream<[Session]>(.hang)
        let viewModel = makeViewModel(source: FakeAgendaSource(sessionsStream: stream), suite: suite)

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
        let suite = Suite()
        defer { suite.cleanup() }
        let first: Set<Room> = [room("r1")]
        let second: Set<Room> = [room("r2"), room("r3")]
        let stream = FakeStream<Set<Room>>(.emit([first, second], thenFinish: true))
        let viewModel = makeViewModel(source: FakeAgendaSource(roomsStream: stream), suite: suite)

        await viewModel.observeRooms()

        #expect(Set((viewModel.roomsContent ?? []).map(\.id)) == ["r2", "r3"])
    }

    @Test func cancelled_rooms_collection_is_silent_and_keeps_the_rooms() async {
        let suite = Suite()
        defer { suite.cleanup() }
        let stream = FakeStream<Set<Room>>(.emit([[room("r1")]], thenFinish: false))
        let viewModel = makeViewModel(source: FakeAgendaSource(roomsStream: stream), suite: suite)

        let collection = Task { @MainActor in await viewModel.observeRooms() }
        await stream.waitUntilSubscribed()
        collection.cancel()
        await collection.value
        await stream.waitUntilTerminated()

        #expect(viewModel.roomsContent?.map(\.id) == ["r1"])
        #expect(stream.terminationCount == 1)
    }

    // MARK: - Persistence on the legacy keys

    @Test func toggling_a_favorite_writes_the_legacy_key_and_notifies_observers() async throws {
        let suite = Suite()
        defer { suite.cleanup() }
        let viewModel = makeViewModel(source: FakeAgendaSource(), suite: suite)
        let session = AgendaContent.Session(from: try #require(try TestData.sessions().first))

        let notified = OSAllocatedUnfairLock(initialState: false)
        withObservationTracking {
            _ = viewModel.favorites
        } onChange: {
            notified.withLock { $0 = true }
        }
        viewModel.toggleBookmark(sessionId: session.id)

        #expect(notified.withLock { $0 })
        #expect(suite.defaults.object(forKey: "Favorites") as? [String] == [session.id])

        viewModel.toggleBookmark(sessionId: session.id)

        #expect(suite.defaults.object(forKey: "Favorites") as? [String] == [])
    }

    @Test func toggle_bookmark_returns_the_post_toggle_value() {
        let suite = Suite()
        defer { suite.cleanup() }
        let viewModel = makeViewModel(source: FakeAgendaSource(), suite: suite)

        #expect(viewModel.toggleBookmark(sessionId: "s1") == true)
        #expect(viewModel.isBookmarked("s1") == true)

        #expect(viewModel.toggleBookmark(sessionId: "s1") == false)
        #expect(viewModel.isBookmarked("s1") == false)
    }

    @Test func favorites_and_filters_are_read_from_the_legacy_keys_and_init_writes_nothing() {
        let seeded = Suite()
        let empty = Suite()
        defer {
            seeded.cleanup()
            empty.cleanup()
        }
        seeded.defaults.set(["a", "b"], forKey: "Favorites")
        let blob = """
        [{"type": "language", "value": "FRENCH"}, {"type": "room", "value": "room-1"}]
        """
        seeded.defaults.set(Data(blob.utf8), forKey: "AgendaSessionFilters")

        let viewModel = makeViewModel(source: FakeAgendaSource(), suite: seeded)
        let fresh = makeViewModel(source: FakeAgendaSource(), suite: empty)

        #expect(viewModel.favorites == ["a", "b"])
        #expect(viewModel.sessionFilters == [
            DevFest_Nantes.SessionFilter(type: .language, value: "FRENCH"),
            DevFest_Nantes.SessionFilter(type: .room, value: "room-1")
        ])
        #expect(fresh.favorites.isEmpty)
        #expect(fresh.sessionFilters.isEmpty)
        #expect(empty.defaults.object(forKey: "Favorites") == nil)
        #expect(empty.defaults.object(forKey: "AgendaSessionFilters") == nil)
    }

    @Test func filters_are_saved_as_json_under_the_legacy_key() throws {
        let suite = Suite()
        defer { suite.cleanup() }
        let viewModel = makeViewModel(source: FakeAgendaSource(), suite: suite)
        let filters: Set<DevFest_Nantes.SessionFilter> = [
            DevFest_Nantes.SessionFilter(type: .bookmark, value: "bookmark"),
            DevFest_Nantes.SessionFilter(type: .complexity, value: "BEGINNER")
        ]

        viewModel.toggleFilter(type: .bookmark, value: "bookmark")
        viewModel.toggleFilter(type: .complexity, value: "BEGINNER")

        let data = try #require(suite.defaults.data(forKey: "AgendaSessionFilters"))
        #expect(try JSONDecoder().decode(Set<DevFest_Nantes.SessionFilter>.self, from: data) == filters)
    }

    @Test func toggling_a_filter_twice_restores_the_previous_set() {
        let suite = Suite()
        defer { suite.cleanup() }
        let viewModel = makeViewModel(source: FakeAgendaSource(), suite: suite)
        let other = DevFest_Nantes.SessionFilter(type: .language, value: "FRENCH")
        viewModel.toggleFilter(type: .language, value: "FRENCH")

        viewModel.toggleFilter(type: .room, value: "room-1")

        #expect(viewModel.sessionFilters == [other, DevFest_Nantes.SessionFilter(type: .room, value: "room-1")])

        viewModel.toggleFilter(type: .room, value: "room-1")

        #expect(viewModel.sessionFilters == [other])
    }

    @Test func clear_filters_empties_the_set_and_removes_the_key() {
        let suite = Suite()
        defer { suite.cleanup() }
        let viewModel = makeViewModel(source: FakeAgendaSource(), suite: suite)
        viewModel.toggleFilter(type: .room, value: "room-1")
        #expect(suite.defaults.data(forKey: "AgendaSessionFilters") != nil)

        viewModel.clearFilters()

        #expect(viewModel.sessionFilters.isEmpty)
        #expect(suite.defaults.object(forKey: "AgendaSessionFilters") == nil)
    }
}
