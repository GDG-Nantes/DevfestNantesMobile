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
    //Store an observable object instance
    @ObservedObject var viewModel: DevFestViewModel
    
    //Property wrapper type that can read and write a value managed by SwiftUI
    @State private var day = "2023-10-19"
    @State private var showFavoritesOnly = false
    @State private var selectedRoom: Room?
    @State private var selectedComplexity: Complexity?
    @State private var selectedLanguage: SessionLanguage?
    @State private var selectedSessionType: SessionType?
    @State private var clearFilter = false
    
    //Section Time formatter
    var sectionTimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
    
    //Setup UI
    var body: some View {
        
        NavigationView {
            LoadingView(isShowing: $viewModel.isLoading) {
                VStack {
                    Picker("What is the day?", selection: $day) {
                        Text(L10n.day1).tag("2023-10-19")
                        Text(L10n.day2).tag("2023-10-20")
                    }
                    .pickerStyle(.segmented)
                    List {
                            ForEach(viewModel.agendaContent.sections.filter{($0.day.contains(day))}, id: \.date) { section in
                                Section(header: Text("\(self.sectionTimeFormatter.string(from: section.date))")) {
                                    let filteredSessions = getFilteredSessions(sessions: section.sessions)
                                    ForEach(self.showFavoritesOnly ? filteredSessions.filter({viewModel.favorites.contains($0.id)}):  filteredSessions, id: \.id) { session in
                                        if session.sessionType == .conference || session.sessionType == .codelab || session.sessionType == .quickie{
                                            NavigationLink(destination: AgendaDetailView(session: session, viewModel: viewModel, day: day)) {
                                                AgendaCellView(viewModel: viewModel, session: session)
                                            }
                                        } else {
                                            AgendaCellView(viewModel: viewModel, session: session)
                                        }
                                    }
                                }
                            }
                    }
                    .navigationBarTitle(L10n.screenAgenda)
                    .navigationBarItems(trailing:
                                            Menu("\(Image(systemName: "line.3.horizontal.decrease.circle"))") {
                        let selected = Binding(
                            get: { self.showFavoritesOnly },
                            set: { self.showFavoritesOnly = $0 == self.showFavoritesOnly ? false : $0 }
                        )
                        Picker("", selection: selected) {
                            Text(L10n.filterFavorites).tag(true)
                            Menu(L10n.sessionFiltersDrawerLanguagesLabel) {
                                let selected = Binding(
                                    get: { self.selectedLanguage },
                                    set: { self.selectedLanguage = $0 == self.selectedLanguage ? nil : $0 })
                                Picker(L10n.sessionFiltersDrawerLanguagesLabel, selection: selected) {
                                    Text(L10n.languageFrench).tag(Optional(SessionLanguage.french))
                                    Text(L10n.languageEnglish).tag(Optional(SessionLanguage.english))
                                }
                            }
                            Menu(L10n.sessionFiltersDrawerComplexityLabel) {
                                let selected = Binding(
                                    get: { self.selectedComplexity },
                                    set: { self.selectedComplexity = $0 == self.selectedComplexity ? nil : $0 })
                                Picker(L10n.sessionFiltersDrawerComplexityLabel, selection: selected) {
                                    Text(L10n.complexityBeginer).tag(Optional(Complexity.beginner))
                                    Text(L10n.complexityIntermediate).tag(Optional(Complexity.intermediate))
                                    Text(L10n.complexityAdvanced).tag(Optional(Complexity.advanced))
                                }
                            }
                            if let rooms = viewModel.roomsContent {
                                Menu(L10n.sessionFiltersDrawerRoomsLabel) {
                                    let selected = Binding(
                                        get: { self.selectedRoom },
                                        set: { self.selectedRoom = $0 == self.selectedRoom ? nil : $0 })
                                    Picker(L10n.sessionFiltersDrawerRoomsLabel, selection: selected) {
                                        ForEach(rooms, id: \.id) { room in
                                            Text(room.name).tag(Optional(room))
                                        }
                                    }
                                }}
                            Menu(L10n.sessionFiltersDrawerTypeLabel) {
                                let selected = Binding(
                                    get: { self.selectedSessionType },
                                    set: { self.selectedSessionType = $0 == self.selectedSessionType ? nil : $0 })
                                Picker(L10n.sessionFiltersDrawerTypeLabel, selection: selected) {
                                    Text(L10n.sessionTypeConference).tag(Optional(SessionType.conference))
                                    Text(L10n.sessionTypeQuickie).tag(Optional(SessionType.quickie))
                                    Text(L10n.sessionTypeCodelab).tag(Optional(SessionType.codelab))
                                }
                            }
                        }
                        ///Adds remove button filters
                        if showFavoritesOnly || selectedRoom != nil || selectedLanguage != nil || selectedComplexity != nil || selectedSessionType != nil {
                            Button(action: {
                                self.showFavoritesOnly = false
                                self.selectedRoom = nil
                                self.selectedLanguage = nil
                                self.selectedSessionType = nil
                                self.selectedComplexity = nil
                            }) {
                                Label(L10n.filterClear, systemImage: "trash")
                            }
                        }
                        
                    })
                    .task {
                        RCValues.sharedInstance.fetchCloudValues()
                        await viewModel.observeRooms()
                        await viewModel.observeSessions()
                    }
                }
            }
        }
        .onAppear{
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.agenda, className: "AgendaView")
        }
    }
    
    ///Method to filter sessions
    private func getFilteredSessions(sessions: [AgendaContent.Session]) -> [AgendaContent.Session]{
        var selectedSession: [AgendaContent.Session] = sessions
        if let unwrappedLanguage = selectedLanguage {
            selectedSession = selectedSession.filter {
                $0.language == unwrappedLanguage
            }
        }
        if let unwrappedSessiontype = selectedSessionType {
            selectedSession = selectedSession.filter {
                $0.sessionType == unwrappedSessiontype
            }
        }
        if let unwrappedRooms = selectedRoom {
            selectedSession = selectedSession.filter({unwrappedRooms.name.contains($0.room)})
        }
        
        if let unwrappedComplexity = selectedComplexity {
            selectedSession = selectedSession.filter {
                $0.complexity == unwrappedComplexity
            }
        }
        return selectedSession
    }
}
