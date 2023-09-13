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
    
    ///Asynchronous method to retrieve partners
    func observePartners() async {
        do {
            let stream = asyncStream(for: store.partnersNative)
            for try await data in stream {
                self.partnersContent = []
                DispatchQueue.main.async {
                    for key in data.keys.sorted() {
                        self.partnersContent?.append(PartnerContent(categoryName: key, partners: data[key]!))
                    }
                }
            }
        } catch {
            Logger.shared.log(.network, .error, "Observe Partners error: \(error)")
        }
    }
}
