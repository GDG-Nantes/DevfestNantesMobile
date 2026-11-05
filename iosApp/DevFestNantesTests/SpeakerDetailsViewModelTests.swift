import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// SpeakerDetails ViewModel tests. Every test builds its own fakes, tracing fake and ViewModel,
/// so nothing is shared between tests.
@MainActor @Suite(.timeLimit(.minutes(1))) struct SpeakerDetailsViewModelTests {

    /// Tracer: the live adapters over the in-memory mocked Kotlin store drive the ViewModel end to end
    /// (no network, Firebase or Koin), and the speaker call goes through the tracing seam under the v1.0
    /// trace name and data source while the sessions call stays untraced.
    @Test func live_source_loads_a_mocked_speaker_and_their_sessions() async throws {
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        var firstSpeakerId: String?
        for try await speakers in LiveSpeakersSource(store: { store }).speakers() {
            firstSpeakerId = speakers.first?.id
            break
        }
        let id = try #require(firstSpeakerId)
        let tracing = RecordingDataLoadTracing()
        let viewModel = SpeakerDetailsViewModel(
            speakerId: id,
            source: LiveSpeakerDetailsSource(store: { store }),
            tracing: tracing
        )

        await viewModel.load()

        #expect(viewModel.speaker?.id == id)
        #expect(viewModel.speakerSession != nil)
        #expect(viewModel.isLoading == false)
        #expect(tracing.recordedTraces == [.init(traceName: "speaker_details_load", dataSource: "graphql")])
    }
}
