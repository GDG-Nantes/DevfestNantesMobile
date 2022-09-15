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

extension Session: Identifiable { }

@MainActor
class DevFestViewModel: ObservableObject {
    let store : DevFestNantesStore
    
    @Published var venueContent: VenueContent = VenueContent(address: "5 rue de Valmy, 44000 Nantes", description: "Située en plein cœur de ville, La Cité des Congrès de Nantes propose pour le DevFest Nantes plus de 3000m² de salles de conférences, codelabs et lieu de rencontre…", latitude: 47.21308725112951, longitude: -1.542622837466317, imageUrl: "https://devfest.gdgnantes.com/static/6328df241501c6e31393e568e5c68d7e/efc43/amphi.webp", name: "Cité des Congrès de Nantes")
    @Published var agendaContent: AgendaContent = AgendaContent(sections: [])
    
    
    init() {
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
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
                let stream = asyncStream(for: store.getVenueNative(language: .french))
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.venueContent = VenueContent(from: data)
                    }
                    
                }
                
            } catch {
                print("Failed with error: \(error)")
            }
        }}


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

    struct VenueContent {
        let address: String
        let description: String
        let latitude: Double
        let longitude: Double
        let imageUrl: String
        let name: String
    }
    
    struct AgendaContent {
        struct Session: Hashable, Identifiable {
            let id: String
            let abstract: String
            let category: shared.Category?
            let language: SessionLanguage?
            let complexity: Complexity?
            let openFeedbackFormId: String
            let speakers: [Speaker]
            let room: String
            let date: Date
            let startDate: String
            let endDate: String
            let durationAndLanguage: String
            let title: String
        }
        
        struct Section: Hashable {
            let date: Date
            let day: String
            var sessions: [Session]
        }
        
        var sections: [Section]
    }

private extension AgendaContent.Session {
    
    init(from session: Session) {
        let newFormatter = ISO8601DateFormatter()
        self.init(id: session.id, abstract: session.abstract, category: session.category, language: session.language, complexity: session.complexity, openFeedbackFormId: session.openFeedbackFormId, speakers: session.speakers, room: session.room?.name ?? "", date: newFormatter.date(from: session.scheduleSlot.startDate) ?? Date(), startDate: session.scheduleSlot.startDate, endDate: session.scheduleSlot.endDate,durationAndLanguage: session.getDurationAndLanguageString(), title: session.title)
    }
}

private extension VenueContent {
    init(from venue: Venue) {
        self.init(address: venue.address, description: venue.description_, latitude: venue.latitude as! Double, longitude: venue.longitude as! Double, imageUrl: venue.imageUrl ?? "https://fakeface.rest/thumb/view", name: venue.name)
    }
}
