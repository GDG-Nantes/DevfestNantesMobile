//
//  SpeakerPicture.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 12/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import URLImage
import shared

struct SpeakerPicture: View {
    var speaker: Speaker_
    
    var body: some View {
        Group {
            if let urlString = speaker.photoUrl, let url = URL(string: urlString) {
                URLImage(url: url) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                }
                .frame(width: 128, height: 128)
                .clipShape(Circle())
            } else {
                Image("person.circle")
                    .resizable()
                    .frame(width: 128, height: 128)
                    .clipShape(Circle())
            }
        }
        .accessibilityLabel("speakerPicture")
    }
}

