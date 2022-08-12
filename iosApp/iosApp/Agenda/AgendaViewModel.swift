//
//  AgendaViewModel.swift
//  iosApp
//
//  Created by Stéphane Rihet on 11/08/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync
import NSLogger

extension Session: Identifiable { }

class AgendaViewModel: ObservableObject, Identifiable {
    let store : DevFestNantesStore
    
    @Published public var sessions: [Session] = []
    
    
    init() {
        self.store = DevFestNantesStoreMocked()
    }
    
    func observeSessions() async {
        do {
            let stream = asyncStream(for: store.sessionsNative)
            for try await data in stream {
                self.sessions = data
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }

}
