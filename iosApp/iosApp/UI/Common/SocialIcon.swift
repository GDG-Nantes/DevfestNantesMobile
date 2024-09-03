//
//  SocialIcon.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 12/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SocialIconsView: View {
    var socials: [SocialItem]
    var onSocialLinkClick: (SocialItem) -> Void  
    
    var body: some View {
        HStack(alignment: .top, spacing: 20) {
            ForEach(socials, id: \.self) { socialItem in
                if let link = socialItem.link, let url = URL(string: link) {
                    Link(destination: url) {
                        if socialItem.type == .twitter {
                            Image("ic_network_twitter")
                                .renderingMode(.template)
                                .foregroundColor(Color(Asset.icColor.color))
                        } else if socialItem.type == .github {
                            Image("ic_network_github")
                                .renderingMode(.template)
                                .foregroundColor(Color(Asset.icColor.color))
                        } else if socialItem.type == .linkedin {
                            Image("ic_network_linkedin")
                                .renderingMode(.template)
                                .foregroundColor(Color(Asset.icColor.color))
                        } else if socialItem.type == .facebook {
                            Image("ic_network_facebook")
                                .renderingMode(.template)
                                .foregroundColor(Color(Asset.icColor.color))
                        } else if socialItem.type == .website {
                            Image("ic_network_web")
                                .renderingMode(.template)
                                .foregroundColor(Color(Asset.icColor.color))
                        }
                    }
                }
            }
        }
    }
}



