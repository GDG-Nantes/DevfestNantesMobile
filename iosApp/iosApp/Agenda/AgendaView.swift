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
    @ObservedObject private var viewModel = AgendaViewModel()
    
    @State private var day = "2022-10-20"
    
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
                    ForEach(viewModel.content.sections.filter{($0.day.contains(day))}, id: \.date) { section in
                        Section(header: Text("\(self.sectionTimeFormatter.string(from: section.date))")) {
                            ForEach(section.sessions, id: \.id) { session in
                                NavigationLink(destination: AgendaDetailView(session: session)) {
                                    AgendaCellView(viewModel: viewModel, session: session)
                                }
                                .listRowBackground( Color(UIColor.systemBackground))
                            }
                        }
                    }
                }
                .navigationBarTitle("Agenda")
                .task {
                    await viewModel.observeSessions()
                }
            }
        }
    }
}

struct AgendaView_Previews: PreviewProvider {
    static var previews: some View {
        AgendaView()
    }
}
