//
//  AgendaDetailView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 11/08/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared
import URLImage


struct AgendaDetailView: View {
    
    var content : AgendaViewModel.Content.Session?
    
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date()// replace Date String
    }
    
    var timeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
    
    var fullDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter
    }()
    
    
    init(session: AgendaViewModel.Content.Session) {
        self.content = session
    }
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(content!.title)
                    .foregroundColor(.red)
                    .font(.title)
                    .padding(.bottom, 8)
                    .padding(.top, 16)
                Text("\(fullDateFormatter.string(from: getDate(date: content!.startDate))), \(timeFormatter.string(from: getDate(date: content!.startDate))) - \(timeFormatter.string(from: getDate(date: content!.endDate))) ")
                    .bold()
                    .font(.headline)
                    .padding(.bottom, 8)
                Text(content!.abstract)
                    .font(.body)
                Divider().padding(.top, 8)
                HStack {
                    LevelView(level: content!.level ?? .beginner)
                }
                Divider().padding(.top, 8)
                ForEach(content!.speakers, id: \.self) { speaker in
                    SpeakerView(speaker: speaker)
                }
            }.padding(.horizontal)
        }
        .navigationBarTitle(Text(content!.title), displayMode: .inline)
    }
}

extension SessionLevel {
    var text: String {
        switch self {
        case .beginner: return "Beginner"
        case .intermediate: return "Intermediate"
        case .expert: return "Expert"
        default:
            return "default"
        }
    }
}

struct LevelView: View {
    var level: SessionLevel
    
    
    var body: some View {
        Text(level.text)
            .font(.body)
            .padding(.vertical, 8)
            .padding(.horizontal, 10)
            .background(Color.secondary)
            .foregroundColor(.black)
            .clipShape(RoundedRectangle(cornerRadius: 35))
    }
}

struct SpeakerView: View {
    var speaker: Speaker
    
    var body: some View {
        
        VStack(alignment: .leading) {
            HStack(alignment: .center) {
                Text("\(speaker.firstname ) \(speaker.surname ?? "surname"), \(speaker.company ?? "Company")")
                    .bold()
                    .padding(.vertical, 24)
                let url = URL(string: speaker.photoUrl ?? "https://fakeface.rest/thumb/view")
                URLImage(url: url!) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .clipShape(Circle())
                }.frame(width: 50, height: 50)
            }
            Text(speaker.bio ?? "bio speaker")
                .padding(.trailing, 8)
            
        }
        .padding(.vertical, 8)
        Divider().padding(.top, 8)
    }
}


//struct AgendaDetailView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaDetailView()
//    }
//}
