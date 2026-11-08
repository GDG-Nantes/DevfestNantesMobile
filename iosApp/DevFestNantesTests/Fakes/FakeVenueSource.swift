import Foundation
import shared
@testable import DevFest_Nantes

/// Hand-written fake of the Venue port: it records the requested languages and language codes, and forwards the
/// venue to a scripted `FakeCall`. The language choice delegates to the real shared use case: it fakes the port,
/// not the rule.
final class FakeVenueSource: VenueSource, @unchecked Sendable {
    let venueCall: FakeCall<Venue>

    private let lock = NSLock()
    private var languages: [ContentLanguage] = []
    private var languageCodes: [String?] = []
    private let venueLanguage = VenueLanguageUseCase()

    init(venueCall: FakeCall<Venue>) {
        self.venueCall = venueCall
    }

    /// Languages requested so far, in call order.
    var requestedLanguages: [ContentLanguage] {
        lock.lock()
        defer { lock.unlock() }
        return languages
    }

    /// Device language codes passed to `language(forCode:)` so far, in call order.
    var requestedLanguageCodes: [String?] {
        lock.lock()
        defer { lock.unlock() }
        return languageCodes
    }

    func venue(language: ContentLanguage) async throws -> Venue {
        lock.withLock { languages.append(language) }
        return try await venueCall.call()
    }

    func language(forCode languageCode: String?) -> ContentLanguage {
        lock.withLock { languageCodes.append(languageCode) }
        return venueLanguage.languageFor(languageCode: languageCode)
    }
}
