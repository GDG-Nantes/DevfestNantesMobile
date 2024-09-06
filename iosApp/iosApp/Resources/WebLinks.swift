//
//  WebLinks.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 02/10/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation

// MARK: - WebLinks
enum WebLinks {
    case website
    case codeOfConduct
    case socialFacebook
    case socialTwitter
    case socialLinkedin
    case socielYoutube
    case nantestechCommunities
    case github
    case openFeedback
    case goTo
}

extension WebLinks {
    //Weblink url
    var url: String {
        switch self {
        case .website:
            return "https://devfest.gdgnantes.com/"
        case .codeOfConduct:
            return "https://devfest.gdgnantes.com/code-of-conduct"
        case .socialFacebook:
            return "https://facebook.com/gdgnantes"
        case .socialTwitter:
            return "https://twitter.com/gdgnantes"
        case .socialLinkedin:
            return "https://www.linkedin.com/in/gdg-nantes"
        case .socielYoutube:
            return "https://www.youtube.com/c/Gdg-franceBlogspotFr"
        case .nantestechCommunities:
            return "https://nantes.community/"
        case .github:
            return "https://github.com/GDG-Nantes/DevfestNantesMobile"
        case .openFeedback:
            return "https://openfeedback.io/devfestnantes24/"
        case .goTo:
            return "http://maps.apple.com/?daddr="
        }
    }
}
