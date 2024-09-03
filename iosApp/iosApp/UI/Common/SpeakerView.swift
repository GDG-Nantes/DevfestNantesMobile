//
//  SpeakerView.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 07/11/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared
import URLImage

///SwiftUI speaker View
struct SpeakerView: View {
    //Speaker data
    var speaker: Speaker_
    
    //setup UI
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
                                if let link = socialItem.link, let url = URL(string: link) {
                                    Link(destination: url) {
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
                                        FirebaseAnalyticsService.shared.eventSpeakerSocialLinkOpened(speakerId: speaker.id, type: socialItem.type)
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
