import Foundation
import shared
@testable import DevFest_Nantes

/// Hand-written fake of the SpeakerDetails port: it records the requested ids and forwards each call to a
/// scripted `FakeCall`.
final class FakeSpeakerDetailsSource: SpeakerDetailsSource, @unchecked Sendable {
    let speakerCall: FakeCall<Speaker?>
    let sessionsCall: FakeCall<[Session]>

    private let lock = NSLock()
    private var speakerIds: [String] = []
    private var sessionsSpeakerIds: [String] = []
    private let classifier = ClassifySessionUseCase()
    private let talkOverride: (@Sendable (Session) -> Bool)?
    private let navigableOverride: (@Sendable (Session) -> Bool)?

    /// Classification delegates to the real shared use case unless a test scripts the two flags.
    init(
        speakerCall: FakeCall<Speaker?>,
        sessionsCall: FakeCall<[Session]>,
        talkOverride: (@Sendable (Session) -> Bool)? = nil,
        navigableOverride: (@Sendable (Session) -> Bool)? = nil
    ) {
        self.speakerCall = speakerCall
        self.sessionsCall = sessionsCall
        self.talkOverride = talkOverride
        self.navigableOverride = navigableOverride
    }

    func isTalk(_ session: Session) -> Bool {
        talkOverride?(session) ?? classifier.isTalk(session: session)
    }

    func isNavigable(_ session: Session) -> Bool {
        navigableOverride?(session) ?? classifier.isNavigable(session: session)
    }

    /// Speaker ids requested through `speaker(id:)` so far, in call order.
    var requestedSpeakerIds: [String] {
        lock.withLock { speakerIds }
    }

    /// Speaker ids requested through `speakerSessions(speakerId:)` so far, in call order.
    var requestedSessionsSpeakerIds: [String] {
        lock.withLock { sessionsSpeakerIds }
    }

    func speaker(id: String) async throws -> Speaker? {
        lock.withLock { speakerIds.append(id) }
        return try await speakerCall.call()
    }

    func speakerSessions(speakerId: String) async throws -> [Session] {
        lock.withLock { sessionsSpeakerIds.append(speakerId) }
        return try await sessionsCall.call()
    }
}
