//
//  SpeakerPicture.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 12/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SpeakerPicture: View {
    var speaker: Speaker_
    
    var body: some View {
        Group {
            if let urlString = speaker.photoUrl, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(width: 128, height: 128)
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure:
                        Image(systemName: "person.circle")
                            .resizable()
                            .frame(width: 128, height: 128)
                            .clipShape(Circle())
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(width: 128, height: 128)
                .clipShape(Circle())
            } else {
                Image(systemName: "person.circle")
                    .resizable()
                    .frame(width: 128, height: 128)
                    .clipShape(Circle())
            }
        }
        .accessibilityLabel("speakerPicture")
    }
}

