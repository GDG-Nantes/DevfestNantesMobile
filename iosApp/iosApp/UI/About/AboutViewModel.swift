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
import NSLogger
import SwiftUI

@MainActor
class AboutViewModel: BaseViewModel {
    @Published var partnersContent: [PartnerContent]?
    
    /// Asynchronous method to retrieve partners
    func observePartners() async {
        self.partnersContent = []

        do {
            let partnersSequence = asyncSequence(for: store.getPartners())
            var newContentArray = [PartnerContent]()
            for try await partners in partnersSequence {
                let sortedKeys = partners.keys.sorted()
                for key in sortedKeys {
                    let newPartnerContent = PartnerContent(categoryName: key, partners: partners[key]!)

                    if !newContentArray.contains(newPartnerContent) {
                        newContentArray.append(newPartnerContent)
                    }
                }
            }
            self.partnersContent = newContentArray
        } catch {
            Logger.shared.log(.network, .error, "Observe Partners error: \(error)")
        }
    }
}

