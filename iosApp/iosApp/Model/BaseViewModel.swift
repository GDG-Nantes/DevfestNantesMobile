//
//  BaseViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

@MainActor
class BaseViewModel: ObservableObject {
    let store: DevFestNantesStore
    

    init(store: DevFestNantesStore = IosDependencies.shared.store()) {
        self.store = store
    }

 
}
