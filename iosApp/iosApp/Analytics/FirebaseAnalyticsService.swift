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

class FirebaseAnalyticsService: AnalyticsService {
    ///Singleton FirebaseAnalytics
    static let shared = FirebaseAnalyticsService()
    
    required internal init() {}
    
    // MARK: - Events
    
    ///Method called when a session is added or removed from favorites.
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
    
    ///Method called when a filter is selected (To Do).
    func eventFilter() {
        //TO DO
    }
    
    ///Method called when clicking on the code of conduct button.
    func eventLinkCodeOfConductOpened() {
        Analytics.logEvent(AnalyticsEvent.linkCodeOfConductOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the DevFest website button.
    func eventLinkDevFestWebsiteOpened() {
        Analytics.logEvent(AnalyticsEvent.linkDevfestWebsiteOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the facebook button.
    func eventLinkFacebookOpened() {
        Analytics.logEvent(AnalyticsEvent.linkFacebookOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the twitter button.
    func eventLinkTwitterOpened() {
        Analytics.logEvent(AnalyticsEvent.linkTwitterOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the github button.
    func eventLinkGithubOpened() {
        Analytics.logEvent(AnalyticsEvent.linkGithubOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the linkedin button.
    func eventLinkLinkedinOpened() {
        Analytics.logEvent(AnalyticsEvent.linkLinkedinOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the local communities button.
    func eventLinkLocalCommunitiesOpened() {
        Analytics.logEvent(AnalyticsEvent.linkLocalCommunitiesOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the partner's logo.
    func eventLinkPartnerOpened(partnerName: String) {
        Analytics.logEvent(AnalyticsEvent.linkPartnerOpened.description(), parameters: [
            AnalyticsParam.partnerName.description(): partnerName
            ]
        )
    }
    
    ///Method called when clicking on the support button.
    func eventLinkSupportOpened() {
        Analytics.logEvent(AnalyticsEvent.linkSupportOpened.description(), parameters: [:] )
    }
    
    ///Method called when clicking on the youtube button.
    func eventLinkYoutubeOpened() {
        Analytics.logEvent(AnalyticsEvent.linkYoutubeOpened.description(), parameters: [:] )
    }

    ///Method called when clicking on the feedback button.
    func eventFeedbackClicked(openFeedbackId: String) {
        Analytics.logEvent(AnalyticsEvent.feedbackClicked.description(), parameters: [
            AnalyticsParam.openfeedbackId.description(): String(openFeedbackId)
            ]
        )
    }
    
    ///Method called when selecting a session.
    func eventSessionOpened(sessionId: String) {
        Analytics.logEvent(AnalyticsEvent.linkPartnerOpened.description(), parameters: [
            AnalyticsParam.sessionId.description(): String(sessionId)
            ]
        )
    }

    func eventSpeakerOpened(speakerId: String) {
        Analytics.logEvent(AnalyticsEvent.speakerOpened.description(), parameters: [
            AnalyticsParam.speakerId.description(): String(speakerId)
            ]
        )
    }
    
    
    ///Method called when a speaker's social network is clicked.
    func eventSpeakerSocialLinkOpened(speakerId: String, type: SocialType) {
        Analytics.logEvent(AnalyticsEvent.speakerSocialLinkOpened.description(), parameters: [
            AnalyticsParam.speakerId.description(): speakerId,
            AnalyticsParam.socialType.description(): type
            ]
        )
    }
    
    ///Method called when the floorplan is clicked.
    func eventVenueFloorPlanClicked() {
        Analytics.logEvent(AnalyticsEvent.venueFloorPlanClicked.description(), parameters: [:])
    }
    
    ///Method called when clicking on the navigation button.
    func eventVenueNavigationClicked() {
        Analytics.logEvent(AnalyticsEvent.venueNavigationClicked.description(), parameters: [:])
    }
    
    // MARK: - Page
    ///Method called when initializing a page.
    func pageEvent(page: AnalyticsPage, className: String?) {
        Analytics.logEvent(AnalyticsEventScreenView,
                           parameters: [AnalyticsParameterScreenName: page.description(), AnalyticsParameterScreenClass: className!])
    }
}

