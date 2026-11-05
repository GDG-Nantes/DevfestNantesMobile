import Foundation
import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Venue port: it records the requested languages and forwards to a scripted `FakeCall`.
final class FakeVenueSource: VenueSource, @unchecked Sendable {
    let venueCall: FakeCall<Venue>

    private let lock = NSLock()
    private var languages: [ContentLanguage] = []

    init(venueCall: FakeCall<Venue>) {
        self.venueCall = venueCall
    }

    /// Languages requested so far, in call order.
    var requestedLanguages: [ContentLanguage] {
        lock.lock()
        defer { lock.unlock() }
        return languages
    }

    func venue(language: ContentLanguage) async throws -> Venue {
        lock.withLock { languages.append(language) }
        return try await venueCall.call()
    }
}
