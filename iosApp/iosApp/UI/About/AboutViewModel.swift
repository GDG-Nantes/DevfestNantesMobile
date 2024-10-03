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
import KMPNativeCoroutinesCombine
import NSLogger
import SwiftUI

@MainActor
class AboutViewModel: BaseViewModel {
    @Published var partnersContent: [PartnerContent]?

    private var cancellables = Set<AnyCancellable>()

    func observePartners() {
        self.partnersContent = []

        let partnersPublisher = createPublisher(for: store.getPartners())

        partnersPublisher
            .sink(receiveCompletion: { completion in
                if case let .failure(error) = completion {
                    Logger.shared.log(.network, .error, "Observe Partners error: \(error)")
                }
            }, receiveValue: { partners in
                var newContentArray = [PartnerContent]()
                let sortedKeys = partners.keys.sorted()
                for key in sortedKeys {
                    let newPartnerContent = PartnerContent(categoryName: key, partners: partners[key]!)
                    newContentArray.append(newPartnerContent)
                }
                self.partnersContent = newContentArray
            })
            .store(in: &self.cancellables)
    }
}
