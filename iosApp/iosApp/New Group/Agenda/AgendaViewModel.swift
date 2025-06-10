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
import KMPNativeCoroutinesAsync
import SwiftUI
import os


class AgendaViewModel: BaseViewModel {
    @Published var agendaContent: AgendaContent = AgendaContent(sections: [])
    @Published var roomsContent: [Room_]?
    @Published var isLoading = true
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
    

    
    ///Asynchronous method to retrieve sessions
    func observeSessions() async {
        do {
            let sessionsSequence = asyncSequence(for: store.getSessions())
            for try await sessions in sessionsSequence {
                DispatchQueue.main.async {
                    self.sessionsChanged(sessions: sessions)
                }
            }
        } catch {
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Agenda").error("Observe Sessions error: \(error.localizedDescription)")
        }
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
    
    ///Asynchronous method to retrieve rooms
    func observeRooms() async {
        Task {
            do {
                let roomsSequence = asyncSequence(for: store.getRooms())
                for try await rooms in roomsSequence {
                    DispatchQueue.main.async {
                        self.roomsContent = Array(rooms)
                    }
                }
            } catch {
                Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Agenda").error("Observe Rooms error: \(error.localizedDescription)")
            }
        }
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

