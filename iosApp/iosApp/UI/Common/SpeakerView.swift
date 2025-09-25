//
//  SpeakerView.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 07/11/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

///SwiftUI speaker View
struct SpeakerView: View {
    //Speaker data
    var speaker: Speaker_
    
    //setup UI
    var body: some View {
        
        VStack(alignment: .leading) {
            HStack(alignment: .top) {
                if let photo = speaker.photoUrl, let url = URL(string: photo) {
                    AsyncImage(url: url) { phase in
                        switch phase {
                        case .empty:
                            ProgressView()
                                .frame(width: 60, height: 60)
                        case .success(let image):
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                                .clipShape(Circle())
                        case .failure:
                            Image(systemName: "person.circle")
                                .resizable()
                                .frame(width: 60, height: 60)
                                .clipShape(Circle())
                        @unknown default:
                            EmptyView()
                        }
                    }
                    .frame(width: 60, height: 60)
                }
                VStack(alignment: .leading) {
                    Text("\(speaker.name), \(speaker.company ?? "")")
                        .bold()
                        .font(.title3)
                    Text(speaker.city ?? "")
                        .bold()
                    Spacer(minLength: 10)
                    Text(speaker.bio ?? "")
                    Spacer(minLength: 10)
                    if let socials = speaker.socials {
                        SocialIconsView(socials: socials) { socialItem in
                            FirebaseAnalyticsService.shared.eventSpeakerSocialLinkOpened(speakerId: speaker.id, type: socialItem.type)
                        }
                    }
                }
            }
            .padding(.vertical, 8)
        }
    }
}