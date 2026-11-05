import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// About ViewModel tests. Every test builds its own stream fake, source and ViewModel, so nothing is shared
/// between tests. The fake serves its scripted native flow through the real `KotlinStreamBridge`.
@MainActor @Suite(.timeLimit(.minutes(1))) struct AboutViewModelTests {

    private struct TestError: Error {}

    private func partner(_ name: String) -> Partner {
        Partner(name: name, logoUrl: nil, url: nil)
    }

    private func makeViewModel(_ stream: FakeStream<[PartnerCategory: [Partner]]>) -> AboutViewModel {
        AboutViewModel(source: FakeAboutSource(partnersStream: stream))
    }

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the ViewModel end to end
    /// through `KotlinStreamBridge` and the store's native flow (no network, Firebase or Koin).
    @Test func live_source_loads_the_mocked_store_partners_through_the_kotlin_bridge() async {
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        let viewModel = AboutViewModel(source: LiveAboutSource(store: { store }))

        await viewModel.observePartners()

        #expect(viewModel.isLoading == false)
        let content = viewModel.partnersContent ?? []
        #expect(!content.isEmpty)
        let ordinals = content.map { $0.categoryName.ordinal }
        #expect(ordinals == ordinals.sorted())
    }

    @Test func loads_only_the_first_emission_and_stops_the_upstream() async {
        let first = partner("first")
        let second = partner("second")
        let stream = FakeStream<[PartnerCategory: [Partner]]>(
            .emit([[.gold: [first]], [.pxl: [second]]], thenFinish: false)
        )
        let viewModel = makeViewModel(stream)

        await viewModel.observePartners()
        await stream.waitUntilTerminated()

        #expect(viewModel.partnersContent == [PartnerContent(categoryName: .gold, partners: [first])])
        #expect(viewModel.isLoading == false)
        #expect(stream.subscriptionCount == 1)
        #expect(stream.terminationCount == 1)
    }

    @Test func lists_categories_by_ordinal_once_each() async {
        var partners: [PartnerCategory: [Partner]] = [:]
        partners[.velotype] = [partner("v")]
        partners[.gold] = [partner("g")]
        partners[.pxl] = [partner("p")]
        let viewModel = makeViewModel(FakeStream(.emit([partners], thenFinish: true)))

        await viewModel.observePartners()

        #expect(viewModel.partnersContent?.map(\.categoryName) == [.pxl, .gold, .velotype])
        #expect(viewModel.isLoading == false)
    }

    @Test func stream_that_ends_without_emission_shows_no_partners() async {
        let viewModel = makeViewModel(FakeStream(.emit([], thenFinish: true)))

        await viewModel.observePartners()

        #expect(viewModel.partnersContent?.isEmpty == true)
        #expect(viewModel.isLoading == false)
    }

    @Test func stream_error_stops_loading_with_empty_content() async {
        let viewModel = makeViewModel(FakeStream(.fail(TestError())))

        await viewModel.observePartners()

        #expect(viewModel.partnersContent?.isEmpty == true)
        #expect(viewModel.isLoading == false)
    }

    @Test func cancelled_load_keeps_the_reset_state() async {
        let stream = FakeStream<[PartnerCategory: [Partner]]>(.hang)
        let viewModel = makeViewModel(stream)

        let load = Task { @MainActor in await viewModel.observePartners() }
        await stream.waitUntilSubscribed()
        load.cancel()
        await load.value
        await stream.waitUntilTerminated()

        #expect(viewModel.isLoading == true)
        #expect(viewModel.partnersContent?.isEmpty == true)
        #expect(stream.terminationCount == 1)
    }

    @Test func each_load_resets_loading_and_content_before_the_new_emission() async {
        let first = partner("first")
        let stream = FakeStream<[PartnerCategory: [Partner]]>(
            .emit([[.gold: [first]]], thenFinish: false),
            .hang
        )
        let viewModel = makeViewModel(stream)

        await viewModel.observePartners()
        await stream.waitUntilSubscribed()
        await stream.waitUntilTerminated()
        #expect(viewModel.partnersContent == [PartnerContent(categoryName: .gold, partners: [first])])
        #expect(viewModel.isLoading == false)

        let reload = Task { @MainActor in await viewModel.observePartners() }
        await stream.waitUntilSubscribed()
        #expect(viewModel.isLoading == true)
        #expect(viewModel.partnersContent?.isEmpty == true)

        reload.cancel()
        await reload.value
        await stream.waitUntilTerminated()
        #expect(viewModel.isLoading == true)
        #expect(viewModel.partnersContent?.isEmpty == true)
        #expect(stream.subscriptionCount == 2)
    }
}
