//
//  AgendaCellView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 12/08/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AgendaCellView: View {
    @ObservedObject var viewModel: AgendaViewModel
    var session: AgendaViewModel.Content.Session
    
    var durationFormatter: DateComponentsFormatter = {
        let formatter = DateComponentsFormatter()
        formatter.unitsStyle = .full
        formatter.allowedUnits = [.minute]
        return formatter
    }()
    
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date()// replace Date String
    }
    
    
    
    var body: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading) {
                Text(session.title)
                    .foregroundColor(.red)
                    .font(.headline)
                    .padding(.bottom, 4)
                Text("\(durationFormatter.string(from: getDate(date: session.startDate) , to: getDate(date: session.endDate) )!)")
                    .font(.footnote)
                Text("\(session.room)")
                    .font(.footnote)
                Text("\(session.language ?? SessionLanguage.english)")
                    .font(.footnote)
                Text(session.speakers.map { $0.name }.joined(separator: ", "))
                    .font(.footnote)
                Spacer()
            }
            Spacer()
            VStack {
                Image(systemName:  "star")
                    .foregroundColor(.yellow)
                    .padding(8)
                Spacer()
            }
        }
    }
}

//struct AgendaCellView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaCellView()
//    }
//}
