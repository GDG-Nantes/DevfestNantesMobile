//
//  AgendaView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AgendaView: View {
    @ObservedObject var viewModel = AgendaViewModel()
    
    @State private var day = "2025-10-16"
    // Use Set<SessionFilter> for all filter state
    @State private var scrollToSectionID: Date? = nil
    @State private var firstAppear = true
    
    var sectionTimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
    
    var body: some View {
        NavigationView {
            LoadingView(isShowing: $viewModel.isLoading) {
                ScrollViewReader { proxy in
                    VStack {
                        Picker("What is the day?", selection: $day) {
                            Text(L10n.day1).tag("2025-10-16")
                            Text(L10n.day2).tag("2025-10-17")
                        }
                        .pickerStyle(SegmentedPickerStyle())

                        List {
                            ForEach(viewModel.agendaContent.sections.filter { $0.day.contains(day) }, id: \.date) { section in
                                let filteredSessions = getFilteredSessions(sessions: section.sessions)
                                let isFavorites = viewModel.sessionFilters.contains { $0.type == .bookmark }
                                let sessionsToShow = isFavorites ? filteredSessions.filter { viewModel.favorites.contains($0.id) } : filteredSessions
                                if !sessionsToShow.isEmpty {
                                    Section(header: Text(sectionTimeFormatter.string(from: section.date)).id(section.date)) {
                                        ForEach(sessionsToShow, id: \.id) { session in
                                            if session.sessionType == .conference || session.sessionType == .codelab || session.sessionType == .quickie {
                                                NavigationLink(destination: AgendaDetailView(session: session, day: day)) {
                                                    AgendaCellView(session: session)
                                                }
                                            } else {
                                                AgendaCellView(session: session)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .onChange(of: scrollToSectionID) { newID in
                            if let newID = newID {
                                proxy.scrollTo(newID, anchor: .top)
                            }
                        }
                    }
                }
            }
            .onChange(of: viewModel.isLoading) { isLoading in
                if !isLoading {
                    let formatter = DateFormatter()
                    formatter.dateFormat = "yyyy-MM-dd"
                    let currentDay = formatter.string(from: Date())
                    
                    if currentDay == day && firstAppear {
                        let calendar = Calendar.current
                        let now = Date()
                        let nowHour = calendar.component(.hour, from: now)
                        let nowMinute = calendar.component(.minute, from: now)
                        let nowTotalMinutes = nowHour * 60 + nowMinute
                        
                        let sortedSections = viewModel.agendaContent.sections
                            .filter({ $0.day.contains(day) })
                            .sorted(by: { section1, section2 in
                                let hour1 = calendar.component(.hour, from: section1.date)
                                let minute1 = calendar.component(.minute, from: section1.date)
                                let totalMinutes1 = hour1 * 60 + minute1
                                
                                let hour2 = calendar.component(.hour, from: section2.date)
                                let minute2 = calendar.component(.minute, from: section2.date)
                                let totalMinutes2 = hour2 * 60 + minute2
                                
                                return totalMinutes1 < totalMinutes2
                            })
                        
                        var previousSection: AgendaContent.Section?
                        
                        for section in sortedSections {
                            let hour = calendar.component(.hour, from: section.date)
                            let minute = calendar.component(.minute, from: section.date)
                            let sectionTotalMinutes = hour * 60 + minute
                            
                            if sectionTotalMinutes <= nowTotalMinutes {
                                previousSection = section
                            } else {
                                break
                            }
                        }
                        
                        if let previousSection = previousSection {
                            DispatchQueue.main.async {
                                scrollToSectionID = previousSection.date
                            }
                        }
                        
                        firstAppear = false
                    }
                }
            }

            .navigationBarTitle(L10n.screenAgenda)
            .navigationBarItems(trailing:
                                    Menu("\(Image(systemName: "line.3.horizontal.decrease.circle"))") {
                // Multi-value filter menus using SessionFilter
                Menu("\(Image(systemName: "line.3.horizontal.decrease.circle"))") {
                    // Favorites filter (single select)
                    Button(action: {
                        var newFilters = viewModel.sessionFilters
                        let filter = SessionFilter(type: .bookmark, value: "bookmark")
                        if newFilters.contains(filter) {
                            newFilters.remove(filter)
                        } else {
                            newFilters.insert(filter)
                        }
                        viewModel.sessionFilters = newFilters
                    }) {
                        Label(L10n.filterFavorites, systemImage: viewModel.sessionFilters.contains { $0.type == .bookmark } ? "star.fill" : "star")
                    }
                    // Language filter (multi-select)
                    Menu(L10n.sessionFiltersDrawerLanguagesLabel) {
                        ForEach([SessionLanguage.french.rawValue, SessionLanguage.english.rawValue], id: \ .self) { lang in
                            Button(action: {
                                var newFilters = viewModel.sessionFilters
                                let filter = SessionFilter(type: .language, value: lang)
                                if newFilters.contains(filter) {
                                    newFilters.remove(filter)
                                } else {
                                    newFilters.insert(filter)
                                }
                                viewModel.sessionFilters = newFilters
                            }) {
                                HStack {
                                    Image(systemName: viewModel.sessionFilters.contains { $0.type == .language && $0.value == lang } ? "checkmark.square" : "square")
                                    Text(lang == SessionLanguage.french.rawValue ? L10n.languageFrench : L10n.languageEnglish)
                                }
                            }
                        }
                    }
                    // Complexity filter (multi-select)
                    Menu(L10n.sessionFiltersDrawerComplexityLabel) {
                        ForEach([Complexity.beginner.rawValue, Complexity.intermediate.rawValue, Complexity.advanced.rawValue], id: \ .self) { comp in
                            Button(action: {
                                var newFilters = viewModel.sessionFilters
                                let filter = SessionFilter(type: .complexity, value: comp)
                                if newFilters.contains(filter) {
                                    newFilters.remove(filter)
                                } else {
                                    newFilters.insert(filter)
                                }
                                viewModel.sessionFilters = newFilters
                            }) {
                                HStack {
                                    Image(systemName: viewModel.sessionFilters.contains { $0.type == .complexity && $0.value == comp } ? "checkmark.square" : "square")
                                    Text(
                                        comp == Complexity.beginner.rawValue ? L10n.complexityBeginer :
                                        comp == Complexity.intermediate.rawValue ? L10n.complexityIntermediate :
                                        L10n.complexityAdvanced
                                    )
                                }
                            }
                        }
                    }
                    // Room filter (multi-select)
                    if let rooms = viewModel.roomsContent {
                        Menu(L10n.sessionFiltersDrawerRoomsLabel) {
                            ForEach(rooms, id: \ .id) { room in
                                Button(action: {
                                    var newFilters = viewModel.sessionFilters
                                    let filter = SessionFilter(type: .room, value: room.id)
                                    if newFilters.contains(filter) {
                                        newFilters.remove(filter)
                                    } else {
                                        newFilters.insert(filter)
                                    }
                                    viewModel.sessionFilters = newFilters
                                }) {
                                    HStack {
                                        Image(systemName: viewModel.sessionFilters.contains { $0.type == .room && $0.value == room.id } ? "checkmark.square" : "square")
                                        Text(room.name)
                                    }
                                }
                            }
                        }
                    }
                    // Session type filter (multi-select)
                    Menu(L10n.sessionFiltersDrawerTypeLabel) {
                        ForEach([SessionType.conference.rawValue, SessionType.quickie.rawValue, SessionType.codelab.rawValue], id: \ .self) { type in
                            Button(action: {
                                var newFilters = viewModel.sessionFilters
                                let filter = SessionFilter(type: .type, value: type)
                                if newFilters.contains(filter) {
                                    newFilters.remove(filter)
                                } else {
                                    newFilters.insert(filter)
                                }
                                viewModel.sessionFilters = newFilters
                            }) {
                                HStack {
                                    Image(systemName: viewModel.sessionFilters.contains { $0.type == .type && $0.value == type } ? "checkmark.square" : "square")
                                    Text(
                                        type == SessionType.conference.rawValue ? L10n.sessionTypeConference :
                                        type == SessionType.quickie.rawValue ? L10n.sessionTypeQuickie :
                                        L10n.sessionTypeCodelab
                                    )
                                }
                            }
                        }
                    }
                    // Clear filters button
                    if !viewModel.sessionFilters.isEmpty {
                        Button(action: {
                            viewModel.clearFilters()
                        }) {
                            Label(L10n.filterClear, systemImage: "trash")
                        }
                    }
                }                
            })
            .task {
                RCValues.sharedInstance.fetchCloudValues()
                await viewModel.observeRooms()
                await viewModel.observeSessions()
            }
        }
        
        .onAppear {
            
            if scrollToSectionID == nil {
                scrollToSectionID = viewModel.agendaContent.sections.first?.date
            }
            
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            let currentDay = formatter.string(from: Date())

            
            if currentDay == "2025-10-16" {
                day = "2025-10-16"
            } else if currentDay == "2025-10-17" {
                day = "2025-10-17"
            } else {
                day = "2025-10-16"
            }
            
            
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.agenda, className: "AgendaView")
        }
    }
    
    private func getFilteredSessions(sessions: [AgendaContent.Session]) -> [AgendaContent.Session] {
        var filtered = sessions
        let selectedLanguages = viewModel.sessionFilters.filter { $0.type == .language }.map { $0.value }
        let selectedSessionTypes = viewModel.sessionFilters.filter { $0.type == .type }.map { $0.value }
        let selectedRooms = viewModel.sessionFilters.filter { $0.type == .room }.map { $0.value }
        let selectedComplexities = viewModel.sessionFilters.filter { $0.type == .complexity }.map { $0.value }

        if !selectedLanguages.isEmpty {
            filtered = filtered.filter { session in
                if let lang = session.language?.name {
                    selectedLanguages.contains(lang)
                } else {
                    false
                }
            }
        }
        if !selectedSessionTypes.isEmpty {
            filtered = filtered.filter { session in
                selectedSessionTypes.contains(session.sessionType?.name ?? "")
            }
        }
        if !selectedRooms.isEmpty {
            filtered = filtered.filter { session in
                selectedRooms.contains(session.room)
            }
        }
        if !selectedComplexities.isEmpty {
            filtered = filtered.filter { session in
                if let comp = session.complexity?.name {
                    selectedComplexities.contains(comp)
                } else {
                    false
                }
            }
        }
        return filtered
    }
}
