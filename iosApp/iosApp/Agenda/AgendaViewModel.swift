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
import SwiftUI
import os


@MainActor
final class AgendaViewModel: ObservableObject {
    @Published var agendaContent: AgendaContent = AgendaContent(sections: [])
    @Published var roomsContent: [Room]?
    @Published var isLoading = true
    let defaults: UserDefaults

    // Filters as Set of SessionFilter
    @Published var sessionFilters: Set<SessionFilter> {
        didSet { saveFilters() }
    }

    private let source: AgendaSource
    private let tracing: DataLoadTracing

    var favorites: [String] {
        get {
            return defaults.object(forKey: "Favorites") as? [String] ?? []
        }
        set {
            defaults.set(newValue, forKey: "Favorites")
        }
    }

    ///Initialization of the model with its ports, the tracing seam and the UserDefaults object (init only reads)
    init(source: AgendaSource = LiveAgendaSource(), tracing: DataLoadTracing = PerformanceMonitoring.shared, defaults: UserDefaults = .standard) {
        self.source = source
        self.tracing = tracing
        self.defaults = defaults
        if let data = defaults.data(forKey: "AgendaSessionFilters"),
           let filters = try? JSONDecoder().decode(Set<SessionFilter>.self, from: data) {
            self.sessionFilters = filters
        } else {
            self.sessionFilters = []
        }
    }

    // MARK: - Persistence helpers
    private func saveFilters() {
        if let data = try? JSONEncoder().encode(sessionFilters) {
            defaults.set(data, forKey: "AgendaSessionFilters")
        }
    }

    func clearFilters() {
        sessionFilters = []
        defaults.removeObject(forKey: "AgendaSessionFilters")
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
        let source = self.source
        do {
            let sessions = try await tracing.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_AGENDA_LOAD,
                dataSource: "graphql"
            ) {
                var first: [Session]?
                // Only the first emission is used; leaving the loop terminates the upstream stream.
                for try await sessionsList in source.sessions() {
                    first = sessionsList
                    break
                }
                // The bridge ends silently when the load is cancelled, so cancellation is checked explicitly.
                try Task.checkCancellation()
                return first ?? []
            }

            sessionsChanged(sessions: sessions)

            DevFestLogger(category: "Agenda").log(.info, "Agenda sessions loaded: \(sessions.count) sessions")

        } catch is CancellationError {
            return
        } catch {
            DevFestLogger(category: "Agenda").log(.error, "Observe Sessions error: \(error.localizedDescription)", error: error)
            isLoading = false
        }
    }

    ///Allows you to classify sessions by time section
    func sessionsChanged(sessions: [Session]) {
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

    ///Asynchronous method to collect rooms until the stream ends or the surrounding task is cancelled
    func observeRooms() async {
        do {
            for try await rooms in source.rooms() {
                roomsContent = Array(rooms)
            }
        } catch is CancellationError {
            return
        } catch {
            DevFestLogger(category: "Agenda").log(.error, "Observe Rooms error: \(error.localizedDescription)", error: error)
        }
    }

    ///Method to convert a date in string format to ISO 8601 format
    func getDate(date: String) -> Date {
        return AgendaViewModel.parseDate(date)
    }

    ///Static so tests can call it without creating a ViewModel
    static func parseDate(_ date: String) -> Date {
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

