//
//  AgendaViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import Observation
import shared

@Observable
@MainActor
final class AgendaViewModel {
    var agendaContent: AgendaContent = AgendaContent(sections: [])
    var roomsContent: [Room]?
    var isLoading = true

    // Observable mirrors of what the Kotlin stores persist. Every intent updates the mirror synchronously
    // and writes through to the store, which is the only place that persists (07 D-15, D-05).
    private(set) var sessionFilters: Set<shared.SessionFilter>
    private(set) var bookmarkedIds: Set<String> = []

    @ObservationIgnored private let source: AgendaSource
    @ObservationIgnored private let tracing: DataLoadTracing
    @ObservationIgnored private var bookmarksSeeded = false

    ///Initialization of the model with its ports and the tracing seam; the filter mirror is read from the store (init writes nothing)
    init(source: AgendaSource = LiveAgendaSource(), tracing: DataLoadTracing = PerformanceMonitoring.shared) {
        self.source = source
        self.tracing = tracing
        self.sessionFilters = source.currentFilters()
    }

    ///Adds the filter when absent and removes it when present, then writes the whole set through to the store
    func toggleFilter(type: SessionFilterType, value: String) {
        let filter = shared.SessionFilter(type: type, value: value)
        var updated = sessionFilters
        if updated.contains(filter) {
            updated.remove(filter)
        } else {
            updated.insert(filter)
        }
        sessionFilters = updated
        source.setFilters(updated)
    }

    ///Empties the filter mirror and writes an empty set through to the store
    func clearFilters() {
        sessionFilters = []
        source.setFilters([])
    }

    // MARK: - Bookmark intents

    ///Whether a session is bookmarked
    func isBookmarked(_ sessionId: String) -> Bool {
        bookmarkedIds.contains(sessionId)
    }

    ///Adds or removes a session from the bookmarks, writes it through to the store and returns the value after the toggle
    @discardableResult
    func toggleBookmark(sessionId: String) -> Bool {
        let bookmarked = !bookmarkedIds.contains(sessionId)
        if bookmarked {
            bookmarkedIds.insert(sessionId)
        } else {
            bookmarkedIds.remove(sessionId)
        }
        source.setBookmarked(sessionId, bookmarked)
        return bookmarked
    }

    ///Seeds the bookmark mirror once per ViewModel from the first emission of the store (it has no snapshot).
    ///A cancelled or failed read leaves the flag false, so the next load retries.
    private func seedBookmarksIfNeeded() async {
        guard !bookmarksSeeded else { return }
        do {
            var iterator = source.bookmarkedIds().makeAsyncIterator()
            guard let ids = try await iterator.next() else { return }
            // The bridge ends silently when the read is cancelled, so cancellation is checked explicitly.
            try Task.checkCancellation()
            bookmarkedIds = ids
            bookmarksSeeded = true
        } catch is CancellationError {
            return
        } catch {
            // Counts only, never the ids.
            DevFestLogger(category: "Agenda").log(.error, "Seed bookmarks error: \(error.localizedDescription)", error: error)
        }
    }

    ///Collects the shared combined flow until the stream ends or the surrounding task is cancelled (D-01).
    ///The trace ends at the first emission while the same subscription keeps running. `isLoading` only ever
    ///goes from true to false (D-03).
    func observeAgenda() async {
        await seedBookmarksIfNeeded()
        var iterator = source.agenda().makeAsyncIterator()
        do {
            let first = try await tracing.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_AGENDA_LOAD,
                dataSource: "graphql"
            ) {
                let first = try await iterator.next()
                // The bridge ends silently when the load is cancelled, so cancellation is checked explicitly.
                try Task.checkCancellation()
                return first
            }

            guard let first else {
                // The flow ended without a value: stop the spinner and keep any previous content.
                isLoading = false
                return
            }
            apply(first)
            let sessionCount = first.flatMap(\.slots).reduce(0) { $0 + $1.sessions.count }
            DevFestLogger(category: "Agenda").log(.info, "Agenda sessions loaded: \(sessionCount) sessions")

            while let next = try await iterator.next() {
                apply(next)
            }
            try Task.checkCancellation()
        } catch is CancellationError {
            return
        } catch {
            DevFestLogger(category: "Agenda").log(.error, "Observe Agenda error: \(error.localizedDescription)", error: error)
            isLoading = false
        }
    }

    private func apply(_ days: [DaySchedule]) {
        agendaContent = AgendaContent(days: days, isTalk: source.isTalk, isNavigable: source.isNavigable)
        isLoading = false
    }

    ///Allows you to classify sessions by time section
    func sessionsChanged(sessions: [Session]) {
        let groupedSessions = Dictionary(grouping: sessions) { getDate(date: $0.scheduleSlot.startDate) }
        let sortedKeys = groupedSessions.keys.sorted()
        var sections = [AgendaContent.Section]()
        sortedKeys.forEach { date in
            let sessions = groupedSessions[date]!
                .map { AgendaContent.Session(from: $0, isATalk: source.isTalk($0), isNavigable: source.isNavigable($0)) }
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
    @ObservationIgnored var sectionDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-d"
        return formatter
    }()
}

