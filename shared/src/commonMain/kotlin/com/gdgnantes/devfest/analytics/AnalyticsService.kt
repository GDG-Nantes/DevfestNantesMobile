package com.gdgnantes.devfest.analytics

import com.gdgnantes.devfest.model.SocialType

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

    fun eventLinkTwitterOpened()

    fun eventLinkYoutubeOpened()

    fun eventNavigationClicked()

    fun eventSessionOpened(sessionId: String)

    fun eventSpeakerSocialLinkOpened(speakerId: String, type: SocialType)

    fun pageEvent(page: AnalyticsPage, className: String? = null)
}