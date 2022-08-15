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
    
    var body: some View {
        NavigationView {
            VStack {
                Picker("What is the day?", selection: $day) {
                    Text("1").tag("2022-10-20")
                    Text("2").tag("2022-10-21")
                }
                .pickerStyle(.segmented)
                List(viewModel.sessions.filter {
                    $0.scheduleSlot.startDate.contains(day)}) { session in
                        NavigationLink(destination: AgendaDetailView(session: session)) {
                            VStack(alignment: .leading) {
                                AgendaCellView(viewModel: viewModel, session: session)
                                Spacer()
                            }
                        }
                    }
                    .navigationTitle("Agenda")
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

