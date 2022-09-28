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
    @ObservedObject var viewModel: DevFestViewModel
    
    @State private var day = "2022-10-20"
    @State private var showFavoritesOnly = false
    @State private var selectedRoom: Room?
        
    var sectionTimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
    
    var body: some View {

            NavigationView {
                LoadingView(isShowing: $viewModel.isLoading) {
                VStack {
                    Picker("What is the day?", selection: $day) {
                        Text(L10n.day1).tag("2022-10-20")
                        Text(L10n.day2).tag("2022-10-21")
                    }
                    .pickerStyle(.segmented)
                    List {
                        ForEach(viewModel.agendaContent.sections.filter{($0.day.contains(day))}, id: \.date) { section in
                            Section(header: Text("\(self.sectionTimeFormatter.string(from: section.date))")) {
                                let filteredSessions = getFilteredSessions(sessions: section.sessions)
                                ForEach(self.showFavoritesOnly ? filteredSessions.filter({viewModel.favorites.contains($0.id)}):  filteredSessions, id: \.id) { session in
                                    if session.sessionType == .conference || session.sessionType == .codelab{
                                        NavigationLink(destination: AgendaDetailView(session: session, viewModel: viewModel)) {
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
                            Menu(L10n.filterRooms) {
                                let selected = Binding(
                                    get: { self.selectedRoom },
                                    set: { self.selectedRoom = $0 == self.selectedRoom ? nil : $0 })
                                Picker(L10n.filterRooms, selection: selected) {
                                    ForEach(self.viewModel.roomsContent, id: \.id) { room in
                                        Text(room.name).tag(Optional(room))
                                    }
                                }
                            }
                        }
                        
                    })
                    .task {
                        await viewModel.observeRooms()
                        await viewModel.observeSessions()
                    }
                }
            }
        }
    }
    
    func getFilteredSessions(sessions: [AgendaContent.Session]) -> [AgendaContent.Session]{
        if let unwrappedRooms = selectedRoom {
            return sessions.filter({unwrappedRooms.name.contains($0.room)})
        }
        return sessions
    }
    
}

//struct AgendaView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaView()
//    }
//}
