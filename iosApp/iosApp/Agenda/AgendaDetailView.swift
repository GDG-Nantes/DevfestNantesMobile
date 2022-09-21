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
    var content : AgendaContent.Session?
    @ObservedObject var viewModel: DevFestViewModel
    
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
    
    
    init(session: AgendaContent.Session, viewModel: DevFestViewModel) {
        self.content = session
        self.viewModel = viewModel
    }
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8) {
                Text(content!.title)
                    .foregroundColor(.red)
                    .font(.title)
                    .padding(.bottom, 8)
                    .padding(.top, 16)
                Text("\(fullDateFormatter.string(from: getDate(date: content!.startDate))), \(timeFormatter.string(from: getDate(date: content!.startDate))) - \(timeFormatter.string(from: getDate(date: content!.endDate))), \(content!.room)")
                    .bold()
                    .font(.headline)
                    .padding(.bottom, 8)
                Divider().padding(.bottom, 8)
                Text("\(content!.durationAndLanguage)")
                    .font(.footnote)
                
                HStack {
                    CategoryView(categoryLabel: content?.category?.label ?? "cat")
                    CategoryView(categoryLabel: content?.complexity?.text ?? "complexity")
                    
                }
                Divider().padding(.top, 8)
                Text(content!.abstract)
                    .font(.body)
                ForEach(content!.speakers, id: \.self) { speaker in
                    SpeakerView(speaker: speaker)
                    Divider().padding(.top, 8)
                }
            }.padding(.horizontal)
        }
        .navigationBarTitle(Text(content!.title), displayMode: .inline)
        .navigationBarItems(trailing:
                                Image(systemName:  viewModel.favorites.contains(content!.id) ? "star.fill" : "star")
            .foregroundColor(.yellow)
            .padding(8)
            .onTapGesture { self.viewModel.toggleFavorite(ofSession: content!)})
    }
}

extension Complexity {
    var text: String {
        switch self {
        case .beginner: return L10n.complexityBeginer
        case .intermediate: return L10n.complexityIntermediate
        case .expert: return L10n.complexityExpert
        default:
            return ""
        }
    }
}

struct SpeakerView: View {
    var speaker: Speaker
    
    var body: some View {
        
        VStack(alignment: .leading) {
            HStack(alignment: .top) {
                let url = URL(string: speaker.photoUrl ?? "https://fakeface.rest/thumb/view")
                URLImage(url: url!) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .clipShape(Circle())
                }.frame(width: 60, height: 60)
                VStack(alignment: .leading) {
                    Text("\(speaker.name ), \(speaker.company ?? "Company")")
                    
                        .bold()
                        .padding(.vertical, 24)
                    Text(speaker.bio ?? "bio speaker")
                        .padding(.trailing, 8)
                    HStack(alignment: .top, spacing: 20) {
                        ForEach(speaker.socials!, id: \.self) { socialItem in
                            Link(destination: URL(string: socialItem.link!)!) {
                                if socialItem.type == .twitter {
                                    Image("ic_network_twitter")
                                } else {
                                    Image("ic_network_web")
                                }
                            }
                        }
                    }
                    
                }
            }
            .padding(.vertical, 8)
        }
    }
}


//struct AgendaDetailView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaDetailView()
//    }
//}
