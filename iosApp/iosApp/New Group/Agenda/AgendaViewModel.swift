//
//  AgendaViewModel.swift
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


class AgendaViewModel: BaseViewModel {
    @Published var agendaContent: AgendaContent = AgendaContent(sections: [])
    @Published var roomsContent: [Room_]?
    @Published var isLoading = true
    private var cancellables = Set<AnyCancellable>()

    let defaults = UserDefaults.standard
    var favorites: [String] {
        get {
            return defaults.object(forKey: "Favorites") as? [String] ?? []
        }
        set {
            defaults.set(newValue, forKey: "Favorites")
        }
    }
    
    ///Initialization of the model with store and the UserDefaults object
    override init() {
        super.init()
        self.favorites = defaults.object(forKey: "Favorites") as? [String] ?? []
    }
    
    // MARK: - Favorites management

    ///Method to add or remove a session from favorites
    func toggleFavorite(ofSession session: AgendaContent.Session) {
        if favorites.contains(session.id) {
            removeSessionFromFavorites(sessionId: session.id)
        } else {
            addSessionToFavorites(sessionId: session.id)
        }
    }
    
    ///Method allowing the deletion of the session in favorites
    private func removeSessionFromFavorites(sessionId: String) {
        objectWillChange.send()
        favorites = favorites.filter { $0 != sessionId }
    }

    private func addSessionToFavorites(sessionId: String) {
        objectWillChange.send()
        favorites.append(sessionId)
    }
    


    func observeSessions() {
        createPublisher(for: store.getSessions())
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                switch completion {
                case .finished:
                    Logger.shared.log(.network, .info, "Sessions observation completed.")
                case .failure(let error):
                    Logger.shared.log(.network, .error, "Observe Sessions error: \(error)")
                }
            }, receiveValue: { sessions in
                Logger.shared.log(.network, .info, "Received sessions: \(sessions)")
                self.sessionsChanged(sessions: sessions)
            })
            .store(in: &cancellables) 
    }


    
    ///Allows you to classify sessions by time section
    private func sessionsChanged(sessions: [Session_]) {
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
        self.isLoading = false
        agendaContent.sections = sections
    }
    
    func observeRooms() {
        createPublisher(for: store.getRooms())
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                switch completion {
                case .finished:
                    Logger.shared.log(.network, .info, "Rooms observation completed.")
                case .failure(let error):
                    Logger.shared.log(.network, .error, "Observe Rooms error: \(error)")
                }
            }, receiveValue: { rooms in
                self.roomsContent = Array(rooms)
            })
            .store(in: &cancellables)
    }

    
    ///Method to convert a date in string format to ISO 8601 format
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date() // replace Date String
    }
    
    ///Formatting the date
    var sectionDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-d"
        return formatter
    }()
}

