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
import KMPNativeCoroutinesAsync
import NSLogger
import SwiftUI

extension Session: Identifiable { }

@MainActor
class DevFestViewModel: ObservableObject {
    ///Properties
    let store : DevFestNantesStore
    let defaults = UserDefaults.standard
    var favorites: [String]
    

    ///Observable objects
    @Published var venueContent: VenueContent?
    @Published var agendaContent: AgendaContent = AgendaContent(sections: [])
    @Published var partnersContent: [PartnerContent]?
    @Published var roomsContent: [Room]?
    @Published var isLoading = true
    
    ///Detect phone language
     var currentLanguage: ContentLanguage {
        guard let languageCode = Locale.current.languageCode else {
            return .english
        }
         if languageCode == "fr" {
             return .french
         }else {
             return .english
         }
            
    }
    
    ///Initialization of the model with store and the UserDefaults object
    init() {
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
        self.favorites = defaults.object(forKey: "Favorites") as? [String] ?? []
    }
    
    ///Asynchronous method to retrieve sessions
    func observeSessions() async {
            do {
//                let stream = asyncSequence(for: store.sessions)
//                for try await data in stream {
//                    DispatchQueue.main.async {
//                        self.sessionsChanged(sessions: data)
//                    }
//                }
            } catch {
                Logger.shared.log(.network, .error, "Observe Sessions error: \(error)")
            }
    }
    
    ///Allows you to classify sessions by time section
    private func sessionsChanged(sessions: [Session]) {
//        let groupedSessions = Dictionary(grouping: sessions) { getDate(date: $0.scheduleSlot.startDate) }
//        let sortedKeys = groupedSessions.keys.sorted()
//        var sections = [AgendaContent.Section]()
//        sortedKeys.forEach { date in
//            let sessions = groupedSessions[date]!
//                .map { AgendaContent.Session(from: $0) }
//                .sorted { $0.room < $1.room }
//            sections.append(AgendaContent.Section(
//                date: date,
//                day: self.sectionDateFormatter.string(from: date),
//                sessions: sessions))
//        }
//        self.isLoading = false
//        agendaContent.sections = sections
    }
    
    ///Asynchronous method to retrieve rooms
    func observeRooms() async {
        Task {
            do {
//                let stream = AsyncSequence(for: store.rooms)
//                for try await data in stream {
//                    DispatchQueue.main.async {
//                        self.roomsContent = Array(data)
//                    }
//                    
//                }
//                
            } catch {
                Logger.shared.log(.network, .error, "Observe Rooms error: \(error)")
            }
        }}
    
    ///Asynchronous method to retrieve venue
    func observeVenue() async {
        Task {
            do {
//                let stream = asyncStream(for: self.store.getVenueNative(language: currentLanguage))
//                for try await data in stream {
//                    DispatchQueue.main.async {
//                        self.venueContent = VenueContent(from: data)
//                    }
//                    
//                }
                
            } catch {
                Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
            }
        }}
    
    ///Asynchronous method to retrieve partners
    func observePartners() async {
        do {
//            let stream = asyncStream(for: store.partnersNative)
//            for try await data in stream {
//                self.partnersContent = []
//                DispatchQueue.main.async {
//                    for key in data.keys.sorted() {
//                        self.partnersContent?.append(PartnerContent(categoryName: key, partners: data[key]!))
//                    }
//                }
//            }
        } catch {
            Logger.shared.log(.network, .error, "Observe Partners error: \(error)")
        }
    }
    
    ///Method to convert a date in string format to ISO 8601 format
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date()// replace Date String
    }
    
    ///Formatting the date
    var sectionDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-d"
        return formatter
    }()
    
}

// MARK: - Favorites management
extension DevFestViewModel {
    
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


