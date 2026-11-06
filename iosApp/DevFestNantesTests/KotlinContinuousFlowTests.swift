import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Research-flag spike of Phase 9: a real, continuous Kotlin flow (the `StateFlow` behind each iOS store)
/// must deliver a second element through `KotlinStreamBridge` after a write, so the agenda ViewModel can
/// rely on the bridge before any ViewModel work starts. The tests use the real Koin stores on the test
/// host's `standardUserDefaults` and restore what they write. A stalled bridge fails through the suite
/// time limit instead of hanging CI.
/// The Kotlin filter type is always written `shared.SessionFilter`: the app still declares its own
/// `SessionFilter` struct.
@MainActor @Suite(.serialized, .timeLimit(.minutes(1))) struct KotlinContinuousFlowTests {

    @Test func filters_flow_delivers_a_second_value_after_a_write() async throws {
        TestHost.start()
        let store: SessionFiltersStore = IosDependencies.shared.sessionFiltersStore()
        let previous = store.filters
        defer { store.setFilters(filters: previous) }
        let spike = shared.SessionFilter(type: .room, value: "spike-room")

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
}
