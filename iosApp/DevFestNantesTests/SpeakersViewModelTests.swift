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

    private struct TestError: Error {}

    private func speaker(_ id: String, _ name: String) -> Speaker {
        Speaker(id: id, bio: nil, company: nil, companyLogoUrl: nil, city: nil, name: name, photoUrl: nil, socials: nil)
    }

    private func makeViewModel(
        _ stream: FakeStream<[Speaker]>,
        tracing: RecordingDataLoadTracing = RecordingDataLoadTracing()
    ) -> SpeakersViewModel {
        SpeakersViewModel(source: FakeSpeakersSource(speakersStream: stream), tracing: tracing)
    }

    @Test func loads_only_the_first_emission_and_records_one_trace() async {
        let first = [speaker("a", "Ada"), speaker("b", "Bob")]
        let second = [speaker("c", "Carol")]
        let stream = FakeStream<[Speaker]>(.emit([first, second], thenFinish: false))
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(stream, tracing: tracing)

        await viewModel.observeSpeakers()
        await stream.waitUntilTerminated()

        #expect(viewModel.speakersContent?.map(\.id) == ["a", "b"])
        #expect(viewModel.isLoading == false)
        #expect(tracing.recordedTraces == [.init(traceName: "speakers_load", dataSource: "graphql")])
        #expect(stream.subscriptionCount == 1)
        #expect(stream.terminationCount == 1)
    }

    @Test func stream_error_stops_loading_without_speakers() async {
        let viewModel = makeViewModel(FakeStream(.fail(TestError())))

        await viewModel.observeSpeakers()

        #expect(viewModel.speakersContent == nil)
        #expect(viewModel.isLoading == false)
    }

    @Test func empty_first_emission_shows_an_empty_alphabet() async {
        let viewModel = makeViewModel(FakeStream(.emit([[]], thenFinish: true)))

        await viewModel.observeSpeakers()

        #expect(viewModel.speakersContent?.isEmpty == true)
        #expect(viewModel.getAlphabets().isEmpty)
        #expect(viewModel.isLoading == false)
    }

    @Test func cancelled_first_load_keeps_the_spinner() async {
        let stream = FakeStream<[Speaker]>(.hang)
        let viewModel = makeViewModel(stream)

        let load = Task { @MainActor in await viewModel.observeSpeakers() }
        await stream.waitUntilSubscribed()
        load.cancel()
        await load.value
        await stream.waitUntilTerminated()

        #expect(viewModel.isLoading == true)
        #expect(viewModel.speakersContent == nil)
        #expect(stream.terminationCount == 1)
    }

    @Test func cancelled_reload_keeps_the_previous_speakers() async {
        let stream = FakeStream<[Speaker]>(
            .emit([[speaker("a", "Ada")]], thenFinish: false),
            .hang
        )
        let viewModel = makeViewModel(stream)

        await viewModel.observeSpeakers()
        await stream.waitUntilSubscribed()
        await stream.waitUntilTerminated()
        #expect(viewModel.speakersContent?.map(\.id) == ["a"])
        #expect(viewModel.isLoading == false)

        let reload = Task { @MainActor in await viewModel.observeSpeakers() }
        await stream.waitUntilSubscribed()
        reload.cancel()
        await reload.value
        await stream.waitUntilTerminated()

        #expect(viewModel.speakersContent?.map(\.id) == ["a"])
        #expect(viewModel.isLoading == false)
        #expect(stream.subscriptionCount == 2)
    }

    @Test func init_does_not_subscribe() {
        let stream = FakeStream<[Speaker]>(.hang)

        _ = makeViewModel(stream)

        #expect(stream.subscriptionCount == 0)
    }
}
