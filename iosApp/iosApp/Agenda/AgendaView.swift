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
            VStack {
                Picker("What is the day?", selection: $day) {
                    Text("Day 1").tag("2022-10-20")
                    Text("Day 2").tag("2022-10-21")
                }
                .pickerStyle(.segmented)
                List {
                    ForEach(viewModel.agendaContent.sections.filter{($0.day.contains(day))}, id: \.date) { section in
                        Section(header: Text("\(self.sectionTimeFormatter.string(from: section.date))")) {
                            let filteredSessions = getFilteredSessions(sessions: section.sessions)
                            ForEach(self.showFavoritesOnly ? filteredSessions.filter({viewModel.favorites.contains($0.id)}):  filteredSessions, id: \.id) { session in
                                NavigationLink(destination: AgendaDetailView(session: session, viewModel: viewModel)) {
                                    AgendaCellView(viewModel: viewModel, session: session)
                                }
                                .listRowBackground( Color(UIColor.systemBackground))
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
                            Text("Favorites").tag(true)
                            Menu("Rooms") {
                                let selected = Binding(
                                    get: { self.selectedRoom },
                                    set: { self.selectedRoom = $0 == self.selectedRoom ? nil : $0 })
                                Picker("Rooms", selection: selected) {
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
    
    func getFilteredSessions(sessions: [AgendaContent.Session]) -> [AgendaContent.Session]{
        if self.selectedRoom != nil {
            return sessions.filter({selectedRoom!.name.contains($0.room)})
        }
        return sessions
    }
    
}

//struct AgendaView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaView()
//    }
//}
