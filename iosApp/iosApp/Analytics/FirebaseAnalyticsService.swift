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
    
    func eventBookmark(page: AnalyticsPage, sessionId: String, bookmarked: Bool) {
        if bookmarked {
            Analytics.logEvent(AnalyticsEvent.addToBookmarks.description(), parameters: [
                AnalyticsParam.fromPage.description(): page.description(),
                AnalyticsParam.sessionId.description(): sessionId
                ]
            )
        } else {
            Analytics.logEvent(AnalyticsEvent.removeFromBookmarks.description(), parameters: [
                AnalyticsParam.fromPage.description(): page.description(),
                AnalyticsParam.sessionId.description(): sessionId
                ]
            )
        }
    }
    
    func eventFilter() {
        
    }
    
    func eventLinkCodeOfConductOpened() {
        Analytics.logEvent(AnalyticsEvent.linkCodeOfConductOpened.description(), parameters: [:] )
    }
    
    func eventLinkDevFestWebsiteOpened() {
        Analytics.logEvent(AnalyticsEvent.linkDevfestWebsiteOpened.description(), parameters: [:] )
    }
    
    func eventLinkFacebookOpened() {
        Analytics.logEvent(AnalyticsEvent.linkFacebookOpened.description(), parameters: [:] )
    }
    
    func eventLinkTwitterOpened() {
        Analytics.logEvent(AnalyticsEvent.linkTwitterOpened.description(), parameters: [:] )
    }
    
    func eventLinkGithubOpened() {
        Analytics.logEvent(AnalyticsEvent.linkGithubOpened.description(), parameters: [:] )
    }
    
    func eventLinkLinkedinOpened() {
        Analytics.logEvent(AnalyticsEvent.linkLinkedinOpened.description(), parameters: [:] )
    }
    
    func eventLinkLocalCommunitiesOpened() {
        Analytics.logEvent(AnalyticsEvent.linkLocalCommunitiesOpened.description(), parameters: [:] )
    }
    
    func eventLinkPartnerOpened(partnerName: String) {
        Analytics.logEvent(AnalyticsEvent.linkPartnerOpened.description(), parameters: [
            AnalyticsParam.partnerName.description(): partnerName
            ]
        )
    }
    
    func eventLinkSupportOpened() {
        Analytics.logEvent(AnalyticsEvent.linkSupportOpened.description(), parameters: [:] )
    }
    
    func eventLinkYoutubeOpened() {
        Analytics.logEvent(AnalyticsEvent.linkYoutubeOpened.description(), parameters: [:] )
    }

    func eventFeedbackClicked(openFeedbackId: String) {
        Analytics.logEvent(AnalyticsEvent.feedbackClicked.description(), parameters: [
            AnalyticsParam.openfeedbackId.description(): String(openFeedbackId)
            ]
        )
    }
    
    func eventSessionOpened(sessionId: String) {
        Analytics.logEvent(AnalyticsEvent.linkPartnerOpened.description(), parameters: [
            AnalyticsParam.sessionId.description(): String(sessionId)
            ]
        )
    }
    
    func eventSpeakerSocialLinkOpened(speakerId: String, type: SocialType) {
        Analytics.logEvent(AnalyticsEvent.speakerSocialLinkOpened.description(), parameters: [
            AnalyticsParam.speakerId.description(): speakerId,
            AnalyticsParam.socialType.description(): type.name
            ]
        )
    }
    
    func eventVenueFloorPlanClicked() {
        Analytics.logEvent(AnalyticsEvent.venueFloorPlanClicked.description(), parameters: [:])
    }
    
    func eventVenueNavigationClicked() {
        Analytics.logEvent(AnalyticsEvent.venueNavigationClicked.description(), parameters: [:])
    }
    
    // MARK: - Page
    
    func pageEvent(page: AnalyticsPage, className: String?) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: page.description(), AnalyticsParameterScreenClass: className!])
    }
}

