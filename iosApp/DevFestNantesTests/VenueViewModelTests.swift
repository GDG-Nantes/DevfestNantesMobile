import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Venue ViewModel tests. Every test builds its own source and ViewModel, so nothing is shared between tests.
@MainActor @Suite(.timeLimit(.minutes(1))) struct VenueViewModelTests {

    private func makeMockedStore() -> DevFestNantesStore {
        DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
    }

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the ViewModel end to end
    /// through the suspend-function bridge (no network, Firebase or Koin).
    @Test func live_source_loads_the_mocked_store_venue_through_the_kotlin_bridge() async throws {
        let store = makeMockedStore()
        let source = LiveVenueSource(store: { store })
        let viewModel = VenueViewModel(source: source)

        await viewModel.observeVenue()

        #expect(viewModel.isLoading == false)
        let content = try #require(viewModel.venueContent)
        #expect(!content.name.isEmpty)
        let fixture = try await source.venue(language: viewModel.currentLanguage)
        #expect(content.description == fixture.description_)
    }
}
