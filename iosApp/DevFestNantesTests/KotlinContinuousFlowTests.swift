import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Research-flag spike of Phase 9: a real, continuous Kotlin flow (the `StateFlow` behind each iOS store)
/// must deliver a second element through `KotlinStreamBridge` after a write, so the agenda ViewModel can
/// rely on the bridge before any ViewModel work starts. The tests use the real Koin stores on the test
/// host's `standardUserDefaults` and restore what they write. A stalled bridge fails through the suite
/// time limit instead of hanging CI.
/// The filter type is the exported Kotlin `SessionFilter`.
/// The suite also hosts the one ViewModel test over the live bookmark store, so its store writes stay
/// serialized with the flow test.
@MainActor @Suite(.serialized, .timeLimit(.minutes(1))) struct KotlinContinuousFlowTests {

    @Test func filters_flow_delivers_a_second_value_after_a_write() async throws {
        TestHost.start()
        let store: SessionFiltersStore = IosDependencies.shared.sessionFiltersStore()
        let previous = store.filters
        defer { store.setFilters(filters: previous) }
        let spike = SessionFilter(type: .room, value: "spike-room")

        var iterator = KotlinStreamBridge.stream(store.filtersFlow).makeAsyncIterator()
        let first = try await iterator.next()
        #expect(first == previous)

        store.setFilters(filters: previous.union([spike]))
        // D-05 premise: the write is visible synchronously, before any element is awaited.
        #expect(store.filters.contains(spike))

        let second = try await iterator.next()
        #expect(second?.contains(spike) == true)
    }

    @Test func bookmarks_flow_delivers_a_second_value_after_a_write() async throws {
        TestHost.start()
        let store: BookmarksStore = IosDependencies.shared.bookmarksStore()
        let id = "spike-\(UUID().uuidString)"
        defer { store.setBookmarked(sessionId: id, bookmarked: false) }

        var iterator = KotlinStreamBridge.stream(store.bookmarkedSessionIds).makeAsyncIterator()
        let first = try await iterator.next()
        #expect(first?.contains(id) == false)

        store.setBookmarked(sessionId: id, bookmarked: true)
        // D-05 premise: the write is visible synchronously, before any element is awaited.
        #expect(store.isBookmarked(id: id))

        let second = try await iterator.next()
        #expect(second?.contains(id) == true)
    }

    @Test func agenda_view_model_reads_a_stored_bookmark_from_the_live_store_before_the_seed() {
        TestHost.start()
        let source = LiveAgendaSource()
        let id = "wr01-\(UUID().uuidString)"
        source.setBookmarked(id, true)
        defer { source.setBookmarked(id, false) }

        // observeAgenda() is never called: the seed was skipped, as on the Speakers path.
        let viewModel = AgendaViewModel(source: LiveAgendaSource(), tracing: RecordingDataLoadTracing())

        #expect(viewModel.isBookmarked(id))
        #expect(viewModel.toggleBookmark(sessionId: id) == false)
        #expect(source.isBookmarked(id) == false)
        #expect(viewModel.isBookmarked(id) == false)
    }
}
