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
        content.map { session in
            ScrollView {
                VStack(alignment: .leading, spacing: 8) {
                    Text(session.title)
                        .foregroundColor(Color(Asset.devFestRed.color))
                        .font(.title)
                        .padding(.bottom, 8)
                        .padding(.top, 16)
                    Text("\(fullDateFormatter.string(from: getDate(date: session.startDate))), \(timeFormatter.string(from: getDate(date: session.startDate))) - \(timeFormatter.string(from: getDate(date: session.endDate))), \(session.room)")
                        .bold()
                        .font(.headline)
                        .padding(.bottom, 8)
                    Divider().padding(.bottom, 8)
                    Text("\(session.durationAndLanguage)")
                        .font(.footnote)
                    
                    HStack {
                        if let categoryLabel = session.category?.label {
                            CategoryView(categoryLabel: categoryLabel)
                        }
                        if let categoryText = session.complexity?.text {
                            CategoryView(categoryLabel:  categoryText)
                        }
                    }
                    Divider().padding(.top, 8)
                    Text(session.abstract)
                        .font(.body)
                    ForEach(session.speakers, id: \.self) { speaker in
                        SpeakerView(speaker: speaker)
                        Divider().padding(.top, 8)
                    }
                }.padding(.horizontal)
            }
            .navigationBarTitle(Text(session.title), displayMode: .inline)
            .navigationBarItems(trailing:
                                    Image(systemName:  viewModel.favorites.contains(session.id) ? "star.fill" : "star")
                .foregroundColor(.yellow)
                .padding(8)
                .onTapGesture { self.viewModel.toggleFavorite(ofSession: session)
                    FirebaseAnalyticsService.shared.eventAddToFavorite(from: .sessionDetails, sessionId: session.id, fav: viewModel.favorites.contains(session.id))
                })
        }
        .onAppear{
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.sessionDetails)
        }
    }
}

extension Complexity {
    var text: String {
        switch self {
        case .beginner: return L10n.complexityBeginer
        case .intermediate: return L10n.complexityIntermediate
        case .advanced: return L10n.complexityAdvanced
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
                if let photo = speaker.photoUrl {
                    URLImage(url: URL(string: photo)!) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                            .clipShape(Circle())
                    }.frame(width: 60, height: 60)
                }
                VStack(alignment: .leading) {
                    Text("\(speaker.name ), \(speaker.company ?? "")")
                        .bold()
                        .font(.title3)
                    Text(speaker.city ?? "")
                        .bold()
                    Spacer(minLength: 10)
                    Text(speaker.bio ?? "")
                    Spacer(minLength: 10)
                    HStack(alignment: .top, spacing: 20) {
                        speaker.socials.map { socials in
                            ForEach(socials, id: \.self) { socialItem in
                                socialItem.link.map { link in
                                    Link(destination: URL(string: link)!) {
                                        if socialItem.type == .twitter {
                                            Image("ic_network_twitter")
                                                .renderingMode(.template)
                                                .foregroundColor(Color(Asset.icColor.color))
                                        } else if socialItem.type == .github  {
                                            Image("ic_network_github")
                                                .renderingMode(.template)
                                                .foregroundColor(Color(Asset.icColor.color))
                                        } else if socialItem.type == .linkedin  {
                                            Image("ic_network_linkedin")
                                                .renderingMode(.template)
                                                .foregroundColor(Color(Asset.icColor.color))
                                        } else if socialItem.type == .facebook  {
                                            Image("ic_network_facebook")
                                                .renderingMode(.template)
                                                .foregroundColor(Color(Asset.icColor.color))
                                        } else if socialItem.type == .website  {
                                            Image("ic_network_web")
                                                .renderingMode(.template)
                                                .foregroundColor(Color(Asset.icColor.color))
                                        }
                                    }.onTapGesture {
                                        FirebaseAnalyticsService.shared.eventSpeakerSocialLinkOpened(speaker: speaker.name, url: link)
                                    }
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
