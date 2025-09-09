//
//  AboutViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesAsync
import SwiftUI
import os

@MainActor
class AboutViewModel: BaseViewModel {
    @Published var partnersContent: [PartnerContent]?
    @Published var isLoading = true
    
    /// Asynchronous method to retrieve partners
    func observePartners() async {
        self.isLoading = true
        self.partnersContent = []

        do {
            let partnersSequence = asyncSequence(for: store.partners)
            var newContentArray = [PartnerContent]()
            // Consommer uniquement la première émission pour éviter une attente infinie
            for try await partners in partnersSequence {
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
            self.partnersContent = newContentArray
            self.isLoading = false
        } catch {
            DevFestLogger(category: "About").log(.error, "Observe Partners error: \(error.localizedDescription)", error: error)
            self.isLoading = false
        }
    }
}

