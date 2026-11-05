//
//  AboutSource.swift
//  DevFest Nantes
//

import shared

/// What the About screen reads: the partners grouped by category.
protocol AboutSource {
    func partners() -> AsyncThrowingStream<[PartnerCategory: [Partner]], Error>
}

/// Live adapter over the shared store. The store is resolved lazily, never in an initializer.
struct LiveAboutSource: AboutSource {
    private let store: () -> DevFestNantesStore

    init(store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() }) {
        self.store = store
    }

    func partners() -> AsyncThrowingStream<[PartnerCategory: [Partner]], Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.partners)
    }
}
