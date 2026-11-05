//
//  VenueSource.swift
//  DevFest Nantes
//

import shared
import KMPNativeCoroutinesAsync

/// What the Venue screen reads: the venue description in a given language.
protocol VenueSource {
    func venue(language: ContentLanguage) async throws -> Venue
}

/// Live adapter over the shared store. The store is resolved lazily, never in an initializer.
struct LiveVenueSource: VenueSource {
    private let store: () -> DevFestNantesStore

    init(store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() }) {
        self.store = store
    }

    func venue(language: ContentLanguage) async throws -> Venue {
        let resolvedStore = store()
        return try await asyncFunction(for: resolvedStore.getVenue(language: language))
    }
}
