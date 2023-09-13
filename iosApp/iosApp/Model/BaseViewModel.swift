//
//  BaseViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared

class BaseViewModel: ObservableObject {
    let store: DevFestNantesStore
    let defaults = UserDefaults.standard
    @Published var favorites: [String] = []

    init() {
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
        self.favorites = defaults.object(forKey: "Favorites") as? [String] ?? []
    }

    // MARK: - Favorites management

    ///Method to add or remove a session from favorites
    func toggleFavorite(ofSession session: AgendaContent.Session) {
        if favorites.contains(session.id) {
            self.removeSessionToFavorite(sessionId: session.id)
        } else {
            self.addSessionToFavorite(sessionId: session.id)
        }
    }
    
    ///Method allowing the deletion of the session in favorites
    private func removeSessionToFavorite(sessionId: String) {
        objectWillChange.send()
        while let idx = favorites.firstIndex(of:sessionId) {
            favorites.remove(at: idx)
        }
        saveFavorites()
    }
    
    ///Method allowing the addition of the session in favorites
    private func addSessionToFavorite(sessionId: String) {
        objectWillChange.send()
        favorites.append(sessionId)
        saveFavorites()
    }
    
    ///Method for saving favorites in userDefaults
    private func saveFavorites() {
        defaults.set(favorites, forKey: "Favorites")
    }
}
