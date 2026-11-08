//
//  VenueSource.swift
//  DevFest Nantes
//

import shared
import KMPNativeCoroutinesAsync

/// What the Venue screen reads: the venue description in a given language, and which language to ask for.
protocol VenueSource {
    func venue(language: ContentLanguage) async throws -> Venue

    /// The content language for a device language code: French for "fr", English for anything else, nil included.
    func language(forCode languageCode: String?) -> ContentLanguage
}

/// Live adapter over the shared store and the shared venue language use case. Both are resolved lazily,
/// never in an initializer.
struct LiveVenueSource: VenueSource {
    private let store: () -> DevFestNantesStore
    private let venueLanguage: () -> VenueLanguageUseCase

    init(
        store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() },
        venueLanguage: @escaping () -> VenueLanguageUseCase = { IosDependencies.shared.venueLanguage() }
    ) {
        self.store = store
        self.venueLanguage = venueLanguage
    }

    func venue(language: ContentLanguage) async throws -> Venue {
        let resolvedStore = store()
        return try await asyncFunction(for: resolvedStore.getVenue(language: language))
    }

    func language(forCode languageCode: String?) -> ContentLanguage {
        venueLanguage().languageFor(languageCode: languageCode)
    }
}
