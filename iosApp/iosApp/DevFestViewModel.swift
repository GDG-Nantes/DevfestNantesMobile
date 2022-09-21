//
//  DevFestViewModel.swift
//  iosApp
//
//  Created by Stéphane Rihet on 14/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync
import NSLogger
import SwiftUI

extension Session: Identifiable { }

@MainActor
class DevFestViewModel: ObservableObject {
    let store : DevFestNantesStore
    let defaults = UserDefaults.standard
    var favorites: [String]
//    var partners: Any
    
    @Published var venueContent: VenueContent = VenueContent(address: "5 rue de Valmy, 44000 Nantes", description: "Située en plein cœur de ville, La Cité des Congrès de Nantes propose pour le DevFest Nantes plus de 3000m² de salles de conférences, codelabs et lieu de rencontre…", latitude: 47.21308725112951, longitude: -1.542622837466317, imageUrl: "https://devfest.gdgnantes.com/static/6328df241501c6e31393e568e5c68d7e/efc43/amphi.webp", name: "Cité des Congrès de Nantes")
    @Published var agendaContent: AgendaContent = AgendaContent(sections: [])
    @Published var partnersContent = [PartnerContent]()
    
    
    init() {
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
        self.favorites = defaults.object(forKey: "Favorites") as? [String] ?? []
    }
    
    func observeSessions() async {
        do {
            let stream = asyncStream(for: store.sessionsNative)
            for try await data in stream {
                DispatchQueue.main.async {
                    self.sessionsChanged(sessions: data)
                }
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }
    
    private func sessionsChanged(sessions: [Session]) {
        let groupedSessions = Dictionary(grouping: sessions) { getDate(date: $0.scheduleSlot.startDate) }
        let sortedKeys = groupedSessions.keys.sorted()
        var sections = [AgendaContent.Section]()
        sortedKeys.forEach { date in
            let sessions = groupedSessions[date]!
                .map { AgendaContent.Session(from: $0) }
                .sorted { $0.room < $1.room }
            sections.append(AgendaContent.Section(
                date: date,
                day: self.sectionDateFormatter.string(from: date),
                sessions: sessions))
        }
        agendaContent.sections = sections
    }
    
    func observeVenue() async {
        Task {
            do {
                let stream = asyncStream(for: self.store.getVenueNative(language: .french))
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.venueContent = VenueContent(from: data)
                    }
                    
                }
                
            } catch {
                print("Failed with error: \(error)")
            }
        }}
    
    func observePartners() async {
        do {
            let stream = asyncStream(for: store.partnersNative)
            for try await data in stream {
                self.partnersContent = []
                DispatchQueue.main.async {
                    for key in data.keys.sorted() {
                        self.partnersContent.append(PartnerContent(categoryName: key, partners: data[key]!))
                    }
                }
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }
    
    
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date()// replace Date String
    }
    
    var sectionDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-d"
        return formatter
    }()
    
}

///Favorites management
extension DevFestViewModel {
    
    func toggleFavorite(ofSession session: AgendaContent.Session) {
        if favorites.contains(session.id) {
            self.removeSessionToFavorite(sessionId: session.id)
        } else {
            self.addSessionToFavorite(sessionId: session.id)
        }
    }
    
    func removeSessionToFavorite(sessionId: String) {
        objectWillChange.send()
        while let idx = favorites.firstIndex(of:sessionId) {
            favorites.remove(at: idx)
        }
        saveFavorites()
    }
    
    func addSessionToFavorite(sessionId: String) {
        objectWillChange.send()
        favorites.append(sessionId)
        saveFavorites()
    }
    
    func saveFavorites() {
        defaults.set(favorites, forKey: "Favorites")
    }
}


