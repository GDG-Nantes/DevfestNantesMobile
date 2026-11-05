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

    init(speakerCall: FakeCall<Speaker?>, sessionsCall: FakeCall<[Session]>) {
        self.speakerCall = speakerCall
        self.sessionsCall = sessionsCall
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
