//
//  AnalyticsEvent.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 28/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation

enum AnalyticsEvent: String {
    case addToFavorite = "addToFavorite"
    case deleteToFavorite = "deleteToFavorite"
    case linkCodeOfConductOpened = "linkCodeOfConductOpened"
    case linkDevFestWebsiteOpened = "linkDevFestWebsiteOpened"
    case linkFacebookOpened = "linkFacebookOpened"
    case linkTwitterOpened = "linkTwitterOpened"
    case linkLinkedinOpened = "linkLinkedinOpened"
    case linkLocalCommunitiesOpened = "localCommunitiesOpened"
    case linkPartnerOpened = "linkPartnerOpened"
    case linkSponsorOpened = "linkSponsorOpened"
    case linkYoutubeOpened = "linkYoutubeOpened"
    case navigationClicked = "navigationClicked"
    case sessionOpened = "sessionOpened"
    case speakerSocialLinkOpened = "speakerSocialLinkOpened"
}
