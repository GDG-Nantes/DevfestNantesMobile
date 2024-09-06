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
    
    /// Asynchronous method to retrieve partners
    func observePartners() async {
        self.partnersContent = []

        do {
            let partnersSequence = asyncSequence(for: store.getPartners())
            var newContentSet = Set<PartnerContent>()
            for try await partners in partnersSequence {
                for key in partners.keys.sorted() {
                    let newPartnerContent = PartnerContent(categoryName: key, partners: partners[key]!)
                    newContentSet.insert(newPartnerContent)
                }
            }
            self.partnersContent = Array(newContentSet)
        } catch {
            Logger.shared.log(.network, .error, "Observe Partners error: \(error)")
        }
    }
}
