//
//  SpeakerDetailsView.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 12/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SpeakerDetails: View {
    @StateObject private var viewModel: SpeakerDetailsViewModel
    var speakerId: String

    init(speakerId: String) {
        self.speakerId = speakerId
        _viewModel = StateObject(wrappedValue: SpeakerDetailsViewModel(speakerId: speakerId))
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .center) {
                if let speaker = viewModel.speaker {
                    SpeakerPicture(speaker: speaker)
                        .frame(width: 128, height: 128)
                    
                    Text(speaker.getFullNameAndCompany())
                        .font(.headline)
                        .fontWeight(.bold)
                        .padding(.top, 8)
                    
                    if let city = speaker.city {
                        Text(city)
                            .font(.subheadline)
                    }
                    
                    if let socials = speaker.socials {
                        SocialIconsView(socials: socials) { socialItem in
                            onSocialLinkClick(socialitem: socialItem)
                        }
                    }
                    
                    if let bio = speaker.bio {
                        Text(bio)
                            .font(.body)
                            .padding(.top, 12)
                    }
                }
                VStack(alignment: .leading) {
                    Text("Talks")
                        .font(.headline)
                        .bold()
                        .padding(.top, 16)
                    
                    if let speakerSessions = viewModel.speakerSession {
                        ForEach(speakerSessions, id: \.id) { session in
                            NavigationLink(destination: AgendaDetailView(session: AgendaContent.Session(from: session), day: "day")) {
                                AgendaCellView(session: AgendaContent.Session(from: session), isBookmarked: false)
                                    .foregroundColor(.primary)
                                    .background(Color.secondary.opacity(0.3))
                                    .cornerRadius(12)
                                    .frame(maxWidth: .infinity)
                            }
                        }
                    }
                }
                
                Spacer()
            }
            .padding(.horizontal)
        }
    }
    
    func onSocialLinkClick(socialitem: SocialItem) {
    }
}




//struct SpeakerDetails_Previews: PreviewProvider {
//    static var previews: some View {
//    }
//}

