import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// About ViewModel tests. Every test builds its own source and ViewModel, so nothing is shared between tests.
@MainActor @Suite struct AboutViewModelTests {

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
}
