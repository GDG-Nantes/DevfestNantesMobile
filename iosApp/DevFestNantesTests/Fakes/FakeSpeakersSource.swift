import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Speakers port: its speakers come from a scripted `FakeStream`
/// (through the real `KotlinStreamBridge`). The grouping delegates to the real `SpeakerIndexUseCase`
/// (a pure class), so the fake stays a fake of the port, not of the rule; it counts the requests.
final class FakeSpeakersSource: SpeakersSource {
    let speakersStream: FakeStream<[Speaker]>
    private(set) var letterGroupRequestCount = 0

    init(speakersStream: FakeStream<[Speaker]>) {
        self.speakersStream = speakersStream
    }

    func speakers() -> AsyncThrowingStream<[Speaker], Error> {
        speakersStream.stream()
    }

    func letterGroups(speakers: [Speaker]) -> [SpeakerLetterGroup] {
        letterGroupRequestCount += 1
        return SpeakerIndexUseCase().letterGroups(speakers: speakers)
    }
}
