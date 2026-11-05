import shared
@testable import DevFest_Nantes

/// Hand-written fake of the About port: its partners come from a scripted `FakeStream`
/// (through the real `KotlinStreamBridge`).
final class FakeAboutSource: AboutSource {
    let partnersStream: FakeStream<[PartnerCategory: [Partner]]>

    init(partnersStream: FakeStream<[PartnerCategory: [Partner]]>) {
        self.partnersStream = partnersStream
    }

    func partners() -> AsyncThrowingStream<[PartnerCategory: [Partner]], Error> {
        partnersStream.stream()
    }
}
