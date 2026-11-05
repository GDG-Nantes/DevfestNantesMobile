import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Venue ViewModel tests. Every test builds its own source and ViewModel, so nothing is shared between tests.
/// The `Venue` value used by the fakes is the fixture of the in-memory mocked Kotlin store, read once per test
/// (building one by hand would need Kotlin number boxes for the coordinates).
@MainActor @Suite(.timeLimit(.minutes(1))) struct VenueViewModelTests {

    private struct TestError: Error {}

    private func makeMockedStore() -> DevFestNantesStore {
        DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
    }

    private func fixtureVenue() async throws -> Venue {
        let store = makeMockedStore()
        return try await LiveVenueSource(store: { store }).venue(language: .english)
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

    @Test func requests_the_venue_in_the_current_language() async throws {
        let source = FakeVenueSource(venueCall: FakeCall(.value(try await fixtureVenue())))
        let viewModel = VenueViewModel(source: source)

        await viewModel.observeVenue()

        #expect(source.requestedLanguages == [viewModel.currentLanguage])
        #expect(viewModel.venueContent != nil)
        #expect(viewModel.isLoading == false)
    }

    @Test func each_load_shows_the_spinner_and_keeps_the_previous_venue() async throws {
        let call = FakeCall<Venue>(.value(try await fixtureVenue()), .suspendUntilCancelled)
        let viewModel = VenueViewModel(source: FakeVenueSource(venueCall: call))

        await viewModel.observeVenue()
        await call.waitUntilCalled()
        let previous = try #require(viewModel.venueContent)
        #expect(viewModel.isLoading == false)

        let reload = Task { @MainActor in await viewModel.observeVenue() }
        await call.waitUntilCalled()
        #expect(viewModel.isLoading == true)
        #expect(viewModel.venueContent?.name == previous.name)
        #expect(viewModel.venueContent?.description == previous.description)

        reload.cancel()
        await reload.value
        #expect(viewModel.isLoading == true)
        #expect(viewModel.venueContent?.name == previous.name)
        #expect(viewModel.venueContent?.description == previous.description)
        #expect(call.callCount == 2)
    }

    @Test func error_stops_loading_and_keeps_the_previous_venue() async throws {
        let call = FakeCall<Venue>(.value(try await fixtureVenue()), .fail(TestError()))
        let viewModel = VenueViewModel(source: FakeVenueSource(venueCall: call))

        await viewModel.observeVenue()
        let previous = try #require(viewModel.venueContent)

        await viewModel.observeVenue()

        #expect(viewModel.venueContent?.name == previous.name)
        #expect(viewModel.venueContent?.description == previous.description)
        #expect(viewModel.isLoading == false)
        #expect(call.callCount == 2)
    }

    @Test func cancelled_first_load_is_silent() async {
        let call = FakeCall<Venue>(.suspendUntilCancelled)
        let viewModel = VenueViewModel(source: FakeVenueSource(venueCall: call))

        let load = Task { @MainActor in await viewModel.observeVenue() }
        await call.waitUntilCalled()
        load.cancel()
        await load.value

        #expect(viewModel.isLoading == true)
        #expect(viewModel.venueContent == nil)
    }

    @Test func init_does_not_call_the_source() {
        let call = FakeCall<Venue>(.suspendUntilCancelled)
        let source = FakeVenueSource(venueCall: call)

        let viewModel = VenueViewModel(source: source)

        #expect(call.callCount == 0)
        #expect(source.requestedLanguages.isEmpty)
        #expect(viewModel.isLoading == true)
        #expect(viewModel.venueContent == nil)
    }
}
