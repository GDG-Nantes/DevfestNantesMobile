import Testing
import Foundation
import os
import Observation
import shared
@testable import DevFest_Nantes

/// Agenda ViewModel tests. Every test builds its own source fake, tracing fake and ViewModel, so nothing is
/// shared between tests. The filter type is the exported Kotlin `SessionFilter`.
@MainActor @Suite(.timeLimit(.minutes(1))) struct AgendaViewModelTests {

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the rooms collection end to end
    /// through `KotlinStreamBridge` and the store's native flows (no network or Firebase). The filter seed
    /// resolves the real Koin stores, so Koin is started first (idempotent); the store only reads here. The live
    /// `agenda()` path is one line over the Koin-bound use case and has no hosted test.
    @Test func live_source_loads_the_mocked_store_rooms() async {
        TestHost.start()
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        let viewModel = AgendaViewModel(source: LiveAgendaSource(store: { store }, classify: { ClassifySessionUseCase() }), tracing: RecordingDataLoadTracing())

        await viewModel.observeRooms()

        #expect(!(viewModel.roomsContent ?? []).isEmpty)
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

    /// What the display mapping makes of the shared output, with a real shared classification.
    private func content(_ days: [DaySchedule]) -> AgendaContent {
        let classifier = ClassifySessionUseCase()
        return AgendaContent(
            days: days,
            isTalk: { classifier.isTalk(session: $0) },
            isNavigable: { classifier.isNavigable(session: $0) }
        )
    }

    private func schedules() throws -> [DaySchedule] {
        try TestData.schedules(zoneId: "Europe/Paris")
    }

    // MARK: - Agenda

    @Test func each_emission_replaces_the_agenda_content() async throws {
        let all = try schedules()
        let first = all
        let second = Array(all.prefix(1))
        let stream = FakeStream<[DaySchedule]>(.emit([first, second], thenFinish: true))
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream), tracing: tracing)

        await viewModel.observeAgenda()

        #expect(!content(second).sections.isEmpty)
        #expect(content(second).sections != content(first).sections)
        #expect(viewModel.agendaContent.sections == content(second).sections)
        #expect(viewModel.isLoading == false)
        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
        #expect(stream.subscriptionCount == 1)
    }

    @Test func rows_take_their_talk_and_navigable_flags_from_the_source() async throws {
        let stream = FakeStream<[DaySchedule]>(.emit([try schedules()], thenFinish: true))
        let source = FakeAgendaSource(
            agendaStream: stream,
            talkOverride: { _ in false }
        )
        let viewModel = makeViewModel(source: source)

        await viewModel.observeAgenda()

        let rows = viewModel.agendaContent.sections.flatMap(\.sessions)
        #expect(!rows.isEmpty)
        #expect(rows.allSatisfy { !$0.isATalk })
        #expect(rows.allSatisfy { $0.isNavigable == ($0.sessionType == .conference || $0.sessionType == .codelab || $0.sessionType == .quickie) })
    }

    @Test func agenda_error_stops_loading_with_an_empty_agenda() async {
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: FakeStream(.fail(TestError()))))

        await viewModel.observeAgenda()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    @Test func agenda_stream_without_emission_stops_loading_with_an_empty_agenda() async {
        let stream = FakeStream<[DaySchedule]>(.emit([], thenFinish: true))
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        await viewModel.observeAgenda()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    @Test func cancelled_agenda_load_keeps_the_spinner() async {
        let stream = FakeStream<[DaySchedule]>(.hang)
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        let load = Task { @MainActor in await viewModel.observeAgenda() }
        await stream.waitUntilSubscribed()
        load.cancel()
        await load.value
        await stream.waitUntilTerminated()

        #expect(viewModel.isLoading == true)
        #expect(viewModel.agendaContent.sections.isEmpty)
    }

    // MARK: - Agenda lifetime and failure (D-01 to D-05)

    /// Returns once the condition holds. Observation-based, no sleep: it returns at once when the condition
    /// already holds, otherwise it suspends until a tracked property changes and checks again.
    private func waitUntil(_ condition: @MainActor @escaping () -> Bool) async {
        while !condition() {
            await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
                withObservationTracking {
                    _ = condition()
                } onChange: {
                    continuation.resume()
                }
            }
        }
    }

    @Test func trace_ends_at_the_first_emission_while_the_stream_stays_open() async throws {
        let stream = FakeStream<[DaySchedule]>(.emit([try schedules()], thenFinish: false))
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream), tracing: tracing)

        let load = Task { @MainActor in await viewModel.observeAgenda() }
        await tracing.waitUntilCompleted()

        #expect(tracing.completedTraceCount == 1)
        #expect(stream.terminationCount == 0)

        load.cancel()
        await load.value
        await stream.waitUntilTerminated()

        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
        #expect(tracing.completedTraceCount == 1)
        #expect(stream.terminationCount == 1)
    }

    @Test func each_subscription_records_one_trace() async throws {
        let all = try schedules()
        let stream = FakeStream<[DaySchedule]>(.emit([all, Array(all.prefix(1))], thenFinish: true))
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream), tracing: tracing)

        await viewModel.observeAgenda()
        await viewModel.observeAgenda()

        #expect(stream.subscriptionCount == 2)
        #expect(tracing.recordedTraces == [
            .init(traceName: "agenda_load", dataSource: "graphql"),
            .init(traceName: "agenda_load", dataSource: "graphql")
        ])
        #expect(tracing.completedTraceCount == 2)
    }

    @Test func resubscribing_keeps_the_previous_content_and_no_spinner() async throws {
        let all = try schedules()
        let stream = FakeStream<[DaySchedule]>(.emit([all], thenFinish: true), .hang)
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        await viewModel.observeAgenda()
        await stream.waitUntilSubscribed()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections == content(all).sections)

        let second = Task { @MainActor in await viewModel.observeAgenda() }
        await stream.waitUntilSubscribed()

        #expect(stream.subscriptionCount == 2)
        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections == content(all).sections)

        second.cancel()
        await second.value

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections == content(all).sections)
    }

    @Test func error_after_an_emission_keeps_the_content_and_stops() async throws {
        let all = try schedules()
        let stream = FakeStream<[DaySchedule]>(.emitThenFail([all], TestError()))
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        await viewModel.observeAgenda()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections == content(all).sections)
        #expect(!viewModel.agendaContent.sections.isEmpty)
        #expect(stream.subscriptionCount == 1)
    }

    @Test func cancelled_collection_is_silent_and_keeps_the_content() async throws {
        let all = try schedules()
        let stream = FakeStream<[DaySchedule]>(.emit([all], thenFinish: false))
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        let collection = Task { @MainActor in await viewModel.observeAgenda() }
        await waitUntil { viewModel.isLoading == false }
        collection.cancel()
        await collection.value
        await stream.waitUntilTerminated()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections == content(all).sections)
        #expect(stream.terminationCount == 1)
    }

    @Test func upstream_cancellation_before_the_first_emission_stops_loading_silently() async {
        let stream = FakeStream<[DaySchedule]>(.cancelUpstream)
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        await viewModel.observeAgenda()

        #expect(viewModel.isLoading == false)
        #expect(viewModel.agendaContent.sections.isEmpty)
        #expect(stream.subscriptionCount == 1)
    }

    @Test func equal_consecutive_emissions_leave_the_content_unchanged() async throws {
        let all = try schedules()
        let stream = FakeStream<[DaySchedule]>(.emit([all, all], thenFinish: true))
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        await viewModel.observeAgenda()

        #expect(!viewModel.agendaContent.sections.isEmpty)
        #expect(viewModel.agendaContent.sections == content(all).sections)
        #expect(sectionSessionIds(viewModel) == content(all).sections.flatMap { $0.sessions.map(\.id) })
        #expect(viewModel.isLoading == false)
    }

    @Test func days_without_slots_show_an_empty_list() async throws {
        let empty = try schedules().map { DaySchedule(dayIndex: $0.dayIndex, date: $0.date, slots: []) }
        let stream = FakeStream<[DaySchedule]>(.emit([empty], thenFinish: false))
        let viewModel = makeViewModel(source: FakeAgendaSource(agendaStream: stream))

        let load = Task { @MainActor in await viewModel.observeAgenda() }
        await waitUntil { viewModel.isLoading == false }
        load.cancel()
        await load.value

        #expect(!empty.isEmpty)
        #expect(viewModel.agendaContent.sections.isEmpty)
        #expect(viewModel.isLoading == false)
    }

    @Test func mirror_stays_correct_while_the_collection_is_cancelled() async throws {
        let stream = FakeStream<[DaySchedule]>(.emit([try schedules()], thenFinish: false))
        let source = FakeAgendaSource(agendaStream: stream)
        let viewModel = makeViewModel(source: source)

        let collection = Task { @MainActor in await viewModel.observeAgenda() }
        await waitUntil { viewModel.isLoading == false }
        collection.cancel()
        await collection.value
        await stream.waitUntilTerminated()

        let bookmarked = viewModel.toggleBookmark(sessionId: "s1")

        #expect(bookmarked == true)
        #expect(viewModel.isBookmarked("s1") == true)
        #expect(source.writtenBookmarks == [.init(sessionId: "s1", bookmarked: true)])
        #expect(stream.subscriptionCount == 1)
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

    private func filter(_ type: SessionFilterType, _ value: String) -> SessionFilter {
        SessionFilter(type: type, value: value)
    }

    @Test func init_seeds_the_filter_mirror_from_the_source_and_writes_nothing() {
        let seeded: Set<SessionFilter> = [filter(.language, "FRENCH"), filter(.room, "room-1")]
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
        let agenda = FakeStream<[DaySchedule]>(.emit([try schedules()], thenFinish: true))
        let bookmarks = FakeStream<Set<String>>(.emit([["a"], ["a", "stale"]], thenFinish: false))
        let source = FakeAgendaSource(agendaStream: agenda, bookmarksStream: bookmarks)
        let viewModel = makeViewModel(source: source)

        await viewModel.observeAgenda()

        #expect(viewModel.bookmarkedIds == ["a"])

        viewModel.toggleBookmark(sessionId: "b")
        await viewModel.observeAgenda()

        #expect(bookmarks.subscriptionCount == 1)
        #expect(viewModel.bookmarkedIds == ["a", "b"])
        #expect(source.writtenBookmarks == [.init(sessionId: "b", bookmarked: true)])
    }

    @Test func empty_stores_start_with_no_bookmarks_and_no_filters() async throws {
        let agenda = FakeStream<[DaySchedule]>(.emit([try schedules()], thenFinish: true))
        let source = FakeAgendaSource(agendaStream: agenda)
        let viewModel = makeViewModel(source: source)

        #expect(viewModel.sessionFilters.isEmpty)
        #expect(viewModel.bookmarkedIds.isEmpty)

        await viewModel.observeAgenda()

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
