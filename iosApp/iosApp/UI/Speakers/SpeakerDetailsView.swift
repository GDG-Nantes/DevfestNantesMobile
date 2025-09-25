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
        LoadingView(isShowing: $viewModel.isLoading) {
            ZStack {
                DevFestSiteBackground()
                ScrollView {
                    VStack(alignment: .center, spacing: 12) {
                        if let speaker = viewModel.speaker {
                            Card {
                                VStack(alignment: .center, spacing: 10) {
                                    SpeakerPicture(speaker: speaker)
                                        .frame(width: 128, height: 128)
                                    
                                    Text(speaker.getFullNameAndCompany())
                                        .font(.headline)
                                        .fontWeight(.bold)
                                        .padding(.top, 4)
                                    
                                    if let city = speaker.city {
                                        Text(city)
                                            .font(.subheadline)
                                            .foregroundStyle(.secondary)
                                    }
                                    
                                    if let socials = speaker.socials {
                                        SocialIconsView(socials: socials) { socialItem in
                                            onSocialLinkClick(socialitem: socialItem)
                                        }
                                    }
                                    
                                    if let bio = speaker.bio {
                                        Text(bio)
                                            .font(.body)
                                            .foregroundStyle(.primary)
                                            .padding(.top, 6)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                            }
                        }
                        
                        Card {
                            VStack(alignment: .leading, spacing: 12) {
                                Text("Talks")
                                    .font(.headline)
                                    .bold()
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                
                                if let speakerSessions = viewModel.speakerSession {
                                    VStack(spacing: 8) {
                                        ForEach(speakerSessions, id: \.id) { session in
                                            NavigationLink(destination: AgendaDetailView(session: AgendaContent.Session(from: session), day: "day")) {
                                                AgendaCellView(session: AgendaContent.Session(from: session), isBookmarked: false)
                                                    .foregroundColor(.primary)
                                                    .frame(maxWidth: .infinity)
                                            }
                                        }
                                    }
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        
                        Spacer(minLength: 8)
                    }
                    .padding(.horizontal)
                }
            }
        }
    }
    
    func onSocialLinkClick(socialitem: SocialItem) {
    }
}