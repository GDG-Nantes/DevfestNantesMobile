import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Speakers port: its speakers come from a scripted `FakeStream`
/// (through the real `KotlinStreamBridge`).
final class FakeSpeakersSource: SpeakersSource {
    let speakersStream: FakeStream<[Speaker]>

    init(speakersStream: FakeStream<[Speaker]>) {
        self.speakersStream = speakersStream
    }

    func speakers() -> AsyncThrowingStream<[Speaker], Error> {
        speakersStream.stream()
    }
}
