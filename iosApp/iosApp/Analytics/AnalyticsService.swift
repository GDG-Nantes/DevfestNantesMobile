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
    
    func eventAddFavorite(from page: AnalyticsPage ,sessionId: String)
    
    func eventLinkCodeOfConductOpened()
    
    func eventLinkDevFestWebsiteOpened()
    
    func eventLinkFacebookOpened()
    
    func eventLinkLinkedinOpened()
    
    func eventLinkLocalCommunitiesOpened()
    
    func eventLinkPartnerOpened(partnerURL: String)
    
    func eventLinkTwitterOpened()
    
    func eventNavigationClicked()

    func eventSessionOpened(sessionId: String)

    func eventSpeakerSocialLinkOpened(speaker: String, url: String)


    // MARK: - Pages

    func pageAgenda(view: any View)
    
    func pageSessionDetails(view: any View)
    
    func pageVenue(view: any View)
    
    func pageAbout(view: any View)

}
