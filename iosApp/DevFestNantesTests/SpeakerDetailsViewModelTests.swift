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

    private struct TestError: Error {}

    private func makeViewModel(
        _ source: FakeSpeakerDetailsSource,
        speakerId: String = "speaker-1",
        tracing: RecordingDataLoadTracing = RecordingDataLoadTracing()
    ) -> SpeakerDetailsViewModel {
        SpeakerDetailsViewModel(speakerId: speakerId, source: source, tracing: tracing)
    }

    @Test func init_starts_no_load() throws {
        let source = FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.value(try TestData.speakers().first)),
            sessionsCall: FakeCall(.value(try TestData.sessions()))
        )

        let viewModel = makeViewModel(source)

        #expect(source.speakerCall.callCount == 0)
        #expect(source.sessionsCall.callCount == 0)
        #expect(viewModel.isLoading == true)
    }

    @Test func load_sets_the_speaker_then_the_sessions_and_stops_loading() async throws {
        let speaker = try #require(try TestData.speakers().first)
        let sessions = try TestData.sessions()
        let source = FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.value(speaker)),
            sessionsCall: FakeCall(.value(sessions))
        )
        let tracing = RecordingDataLoadTracing()
        let viewModel = makeViewModel(source, speakerId: speaker.id, tracing: tracing)

        await viewModel.load()

        #expect(viewModel.speaker?.id == speaker.id)
        #expect(viewModel.speakerSession?.count == sessions.count)
        #expect(viewModel.isLoading == false)
        #expect(source.requestedSpeakerIds == [speaker.id])
        #expect(source.requestedSessionsSpeakerIds == [speaker.id])
        #expect(tracing.recordedTraces == [.init(traceName: "speaker_details_load", dataSource: "graphql")])
    }

    @Test func speaker_is_set_while_the_sessions_are_still_loading() async throws {
        let speaker = try #require(try TestData.speakers().first)
        let source = FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.value(speaker)),
            sessionsCall: FakeCall(.suspendUntilCancelled)
        )
        let viewModel = makeViewModel(source, speakerId: speaker.id)

        let load = Task { @MainActor in await viewModel.load() }
        await source.sessionsCall.waitUntilCalled()

        #expect(viewModel.speaker?.id == speaker.id)
        #expect(viewModel.speakerSession == nil)
        #expect(viewModel.isLoading == true)

        load.cancel()
        await load.value
    }

    @Test func speaker_error_stops_loading_and_still_requests_the_sessions() async throws {
        let sessions = try TestData.sessions()
        let source = FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.fail(TestError())),
            sessionsCall: FakeCall(.value(sessions))
        )
        let viewModel = makeViewModel(source)

        await viewModel.load()

        #expect(viewModel.speaker == nil)
        #expect(viewModel.speakerSession?.count == sessions.count)
        #expect(viewModel.isLoading == false)
        #expect(source.sessionsCall.callCount == 1)
    }

    @Test func sessions_error_stops_loading_and_keeps_the_speaker() async throws {
        let speaker = try #require(try TestData.speakers().first)
        let source = FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.value(speaker)),
            sessionsCall: FakeCall(.fail(TestError()))
        )
        let viewModel = makeViewModel(source, speakerId: speaker.id)

        await viewModel.load()

        #expect(viewModel.speaker?.id == speaker.id)
        #expect(viewModel.speakerSession == nil)
        #expect(viewModel.isLoading == false)
    }

    @Test func content_for_a_session_carries_the_shared_classification() throws {
        let sessions = try TestData.sessions()
        let talk = try #require(sessions.first { $0.type == .conference })
        let service = try #require(sessions.first { $0.type == .opening || $0.type == .lunch || $0.type == .break_ })
        let classifier = ClassifySessionUseCase()

        // Real shared use case behind the port: the flags are the use case's, the session is unchanged.
        let live = makeViewModel(FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.value(nil)),
            sessionsCall: FakeCall(.value([]))
        ))
        for session in [talk, service] {
            let content = live.content(for: session)
            #expect(content.isATalk == classifier.isTalk(session: session), "isATalk of \(session.id)")
            #expect(content.isNavigable == classifier.isNavigable(session: session), "isNavigable of \(session.id)")
            #expect(content.id == session.id)
            #expect(content.title == session.title)
        }
        #expect(live.content(for: talk).isATalk == true)
        #expect(live.content(for: talk).isNavigable == true)
        #expect(live.content(for: service).isATalk == false)
        #expect(live.content(for: service).isNavigable == false)

        // Scripted port, opposite to the v1.0 rules: the ViewModel takes the flags from its source only.
        let scripted = makeViewModel(FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.value(nil)),
            sessionsCall: FakeCall(.value([])),
            talkOverride: { _ in false },
            navigableOverride: { _ in false }
        ))
        #expect(scripted.content(for: talk).isATalk == false)
        #expect(scripted.content(for: talk).isNavigable == false)
        #expect(scripted.content(for: talk).id == talk.id)
    }

    @Test func cancellation_during_the_speaker_call_is_silent() async throws {
        let source = FakeSpeakerDetailsSource(
            speakerCall: FakeCall(.suspendUntilCancelled),
            sessionsCall: FakeCall(.value(try TestData.sessions()))
        )
        let viewModel = makeViewModel(source)

        let load = Task { @MainActor in await viewModel.load() }
        await source.speakerCall.waitUntilCalled()
        load.cancel()
        await load.value

        #expect(viewModel.speaker == nil)
        #expect(viewModel.speakerSession == nil)
        #expect(viewModel.isLoading == true)
        #expect(source.sessionsCall.callCount == 0)
    }
}
