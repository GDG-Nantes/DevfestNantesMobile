//
//  FirebaseAnalyticsService.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 28/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import Firebase

class FirebaseAnalyticsService: AnalyticsService {
    
    // MARK: - Events
    
    func eventAddFavorite(from page: AnalyticsPage, sessionId: String) {
        Analytics.logEvent(AnalyticsEvent.speakerSocialLinkOpened.rawValue, parameters: [
            AnalyticsParam.fromPage.rawValue: page.rawValue,
            AnalyticsParam.sessionId.rawValue: sessionId
            ]
        )
    }
    
    func eventLinkCodeOfConductOpened() {
        Analytics.logEvent(AnalyticsEvent.linkCodeOfConductOpened.rawValue, parameters: [:] )
    }
    
    func eventLinkDevFestWebsiteOpened() {
        Analytics.logEvent(AnalyticsEvent.linkDevFestWebsiteOpened.rawValue, parameters: [:] )
    }
    
    func eventLinkFacebookOpened() {
        Analytics.logEvent(AnalyticsEvent.linkFacebookOpened.rawValue, parameters: [:] )
    }
    
    func eventLinkTwitterOpened() {
        Analytics.logEvent(AnalyticsEvent.linkTwitterOpened.rawValue, parameters: [:] )
    }
    
    
    func eventLinkLinkedinOpened() {
        Analytics.logEvent(AnalyticsEvent.linkLinkedinOpened.rawValue, parameters: [:] )
    }
    
    func eventLinkLocalCommunitiesOpened() {
        Analytics.logEvent(AnalyticsEvent.linkLocalCommunitiesOpened.rawValue, parameters: [:] )
    }
    
    func eventLinkPartnerOpened(partnerURL: String) {
        Analytics.logEvent(AnalyticsEvent.linkPartnerOpened.rawValue, parameters: [
            AnalyticsParam.partnerURL.rawValue: String(partnerURL)
            ]
        )
    }
    
    func eventNavigationClicked() {
        Analytics.logEvent(AnalyticsEvent.navigationClicked.rawValue, parameters: [:])
    }
    
    func eventSessionOpened(sessionId: String) {
        Analytics.logEvent(AnalyticsEvent.linkPartnerOpened.rawValue, parameters: [
            AnalyticsParam.sessionId.rawValue: String(sessionId)
            ]
        )
    }
    
    func eventSpeakerSocialLinkOpened(speaker: String, url: String) {
        Analytics.logEvent(AnalyticsEvent.speakerSocialLinkOpened.rawValue, parameters: [
            AnalyticsParam.speaker.rawValue: speaker,
            AnalyticsParam.socialLink.rawValue: url
            ]
        )
    }
    
    // MARK: - Page
    
    func pageAgenda(view: any View) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: AnalyticsPage.agenda.rawValue, AnalyticsParameterScreenClass: "\(type(of: view.self))"])
    }
    
    func pageSessionDetails(view: any View) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: AnalyticsPage.sessionDetails.rawValue, AnalyticsParameterScreenClass: "\(type(of: view.self))"])
    }
    
    func pageVenue(view: any View) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: AnalyticsPage.venue.rawValue, AnalyticsParameterScreenClass: "\(type(of: view.self))"])
    }
    
    func pageAbout(view: any View) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: AnalyticsPage.about.rawValue, AnalyticsParameterScreenClass: "\(type(of: view.self))"])
    }
}

