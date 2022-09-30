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
import shared

class FirebaseAnalyticsService: AnalyticsService{
    
    static let shared = FirebaseAnalyticsService()
    
    required internal init() {}
    // MARK: - Events
    
    func eventAddToFavorite(page: AnalyticsPage, sessionId: String, fav: Bool) {
        if fav {
            Analytics.logEvent(AnalyticsEvent.addtofavorite.name, parameters: [
                AnalyticsParam.frompage.name: page.name,
                AnalyticsParam.sessionid.name: sessionId
                ]
            )
        } else {
            Analytics.logEvent(AnalyticsEvent.deletetofavorite.name, parameters: [
                AnalyticsParam.frompage.name: page.name,
                AnalyticsParam.sessionid.name: sessionId
                ]
            )
        }
    }
    
    func eventLinkCodeOfConductOpened() {
        Analytics.logEvent(AnalyticsEvent.linkcodeofconductopened.name, parameters: [:] )
    }
    
    func eventLinkDevFestWebsiteOpened() {
        Analytics.logEvent(AnalyticsEvent.linkdevfestwebsiteopened.name, parameters: [:] )
    }
    
    func eventLinkFacebookOpened() {
        Analytics.logEvent(AnalyticsEvent.linkfacebookopened.name, parameters: [:] )
    }
    
    func eventLinkTwitterOpened() {
        Analytics.logEvent(AnalyticsEvent.linktwitteropened.name, parameters: [:] )
    }
    
    
    func eventLinkLinkedinOpened() {
        Analytics.logEvent(AnalyticsEvent.linklinkedinopened.name, parameters: [:] )
    }
    
    func eventLinkLocalCommunitiesOpened() {
        Analytics.logEvent(AnalyticsEvent.linklocalcommunitiesopened.name, parameters: [:] )
    }
    
    func eventLinkPartnerOpened(partnerURL: String) {
        Analytics.logEvent(AnalyticsEvent.linkpartneropened.name, parameters: [
            AnalyticsParam.partnerurl.name: String(partnerURL)
            ]
        )
    }
    
    func eventLinkYoutubeOpened() {
        Analytics.logEvent(AnalyticsEvent.linkyoutubeopened.name, parameters: [:] )
    }

    
    func eventNavigationClicked() {
        Analytics.logEvent(AnalyticsEvent.navigationclicked.name, parameters: [:])
    }
    
    func eventSessionOpened(sessionId: String) {
        Analytics.logEvent(AnalyticsEvent.linkpartneropened.name, parameters: [
            AnalyticsParam.sessionid.name: String(sessionId)
            ]
        )
    }
    
    func eventSpeakerSocialLinkOpened(speaker: String, url: String) {
        Analytics.logEvent(AnalyticsEvent.speakersociallinkopened.name, parameters: [
            AnalyticsParam.speaker.name: speaker,
            AnalyticsParam.sociallink.name: url
            ]
        )
    }
    
    // MARK: - Page
    
    func pageEvent(page: AnalyticsPage) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: page.name])
    }
}

