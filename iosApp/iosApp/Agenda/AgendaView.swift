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
                            ForEach(self.showFavoritesOnly ? section.sessions.filter({viewModel.favorites.contains($0.id)}):  section.sessions, id: \.id) { session in
                                NavigationLink(destination: AgendaDetailView(session: session, viewModel: viewModel)) {
                                    AgendaCellView(viewModel: viewModel, session: session)
                                }
                                .listRowBackground( Color(UIColor.systemBackground))
                            }
                        }
                    }
                }
                .navigationBarTitle("Agenda")
                .navigationBarItems(trailing:
                                        Button(action: { self.showFavoritesOnly.toggle() }) {
                    Image(systemName: showFavoritesOnly ? "star.fill" : "star").padding()
                }
                )
                .task {
                    await viewModel.observeSessions()
                }
            }
        }
    }
}

//struct AgendaView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaView()
//    }
//}
