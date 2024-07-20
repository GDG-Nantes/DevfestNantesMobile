package com.gdgnantes.devfest.analytics

import com.gdgnantes.devfest.model.SocialType

@Suppress("TooManyFunctions")
interface AnalyticsService {
    fun eventBookmark(page: AnalyticsPage, sessionId: String, bookmarked: Boolean)

    fun eventFilter()

    fun eventLinkCodeOfConductOpened()

    fun eventLinkDevFestWebsiteOpened()

    fun eventLinkFacebookOpened()

    fun eventLinkGithubOpened()

    fun eventLinkLinkedinOpened()

    fun eventLinkLocalCommunitiesOpened()

    fun eventLinkPartnerOpened(partnerName: String)

    fun eventLinkSupportOpened()

    fun eventLinkTwitterOpened()

    fun eventLinkYoutubeOpened()

    fun eventFeedbackClicked(openFeedbackId: String)

    fun eventSessionOpened(sessionId: String)

    fun eventSpeakerOpened(speakerId: String)

    fun eventSpeakerSocialLinkOpened(speakerId: String, type: SocialType)

    fun eventVenueNavigationClicked()

    fun eventVenueFloorPlanClicked()

    fun pageEvent(page: AnalyticsPage, className: String? = null)
}
