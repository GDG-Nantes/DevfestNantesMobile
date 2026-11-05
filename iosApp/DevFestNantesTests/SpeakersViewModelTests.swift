import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Speakers ViewModel tests. Every test builds its own stream fake, source, tracing fake and ViewModel,
/// so nothing is shared between tests. The fake serves its scripted native flow through the real
/// `KotlinStreamBridge`.
@MainActor @Suite(.timeLimit(.minutes(1))) struct SpeakersViewModelTests {

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the ViewModel end to end
    /// through `KotlinStreamBridge` and the store's native flow (no network, Firebase or Koin), and the
    /// load goes through the tracing seam under the v1.0 trace name and data source.
    @Test func live_source_loads_the_mocked_store_speakers_and_records_the_speakers_trace() async {
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        let tracing = RecordingDataLoadTracing()
        let viewModel = SpeakersViewModel(source: LiveSpeakersSource(store: { store }), tracing: tracing)

        await viewModel.observeSpeakers()

        #expect(viewModel.isLoading == false)
        #expect(!(viewModel.speakersContent ?? []).isEmpty)
        #expect(!viewModel.getAlphabets().isEmpty)
        #expect(tracing.recordedTraces == [.init(traceName: "speakers_load", dataSource: "graphql")])
    }
}
