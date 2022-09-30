//
//  AnalyticsService.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 28/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI

protocol AnalyticsService {

    // MARK: - Events
    
    func eventAddToFavorite(from page: AnalyticsPage, sessionId: String, fav: Bool)
    
    func eventLinkCodeOfConductOpened()
    
    func eventLinkDevFestWebsiteOpened()
    
    func eventLinkFacebookOpened()
    
    func eventLinkLinkedinOpened()
    
    func eventLinkLocalCommunitiesOpened()
    
    func eventLinkPartnerOpened(partnerURL: String)
    
    func eventLinkTwitterOpened()
    
    func eventLinkYoutubeOpened()
    
    func eventNavigationClicked()

    func eventSessionOpened(sessionId: String)

    func eventSpeakerSocialLinkOpened(speaker: String, url: String)


    // MARK: - Pages

    func pageEvent(page: AnalyticsPage)

}
