//
//  AboutViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import Observation
import shared

@Observable
@MainActor
final class AboutViewModel {
    var partnersContent: [PartnerContent]?
    var isLoading = true

    @ObservationIgnored private let source: AboutSource

    init(source: AboutSource = LiveAboutSource()) {
        self.source = source
    }

    /// Asynchronous method to retrieve partners
    func observePartners() async {
        isLoading = true
        partnersContent = []

        do {
            var newContentArray = [PartnerContent]()
            // Only the first emission is used; leaving the loop terminates the upstream stream.
            for try await partners in source.partners() {
                let sortedKeys = partners.keys.sorted()
                for key in sortedKeys {
                    if let list = partners[key] {
                        let newPartnerContent = PartnerContent(categoryName: key, partners: list)
                        if !newContentArray.contains(newPartnerContent) {
                            newContentArray.append(newPartnerContent)
                        }
                    }
                }
                break
            }
            // The bridge ends silently when the load is cancelled, so cancellation is checked explicitly.
            try Task.checkCancellation()
            partnersContent = newContentArray
            isLoading = false
        } catch is CancellationError {
            return
        } catch {
            DevFestLogger(category: "About").log(.error, "Observe Partners error: \(error.localizedDescription)", error: error)
            isLoading = false
        }
    }
}
