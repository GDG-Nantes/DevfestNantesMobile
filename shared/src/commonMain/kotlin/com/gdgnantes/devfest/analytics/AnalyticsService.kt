package com.gdgnantes.devfest.analytics

interface AnalyticsService {

    fun eventBookmark(page: AnalyticsPage, sessionId: String, fav: Boolean)

    fun eventFilter()

    fun eventLinkCodeOfConductOpened()

    fun eventLinkDevFestWebsiteOpened()

    fun eventLinkFacebookOpened()

    fun eventLinkLinkedinOpened()

    fun eventLinkLocalCommunitiesOpened()

    fun eventLinkPartnerOpened(partnerURL: String)

    fun eventLinkTwitterOpened()

    fun eventLinkYoutubeOpened()

    fun eventNavigationClicked()

    fun eventSessionOpened(sessionId: String)

    fun eventSpeakerSocialLinkOpened(speaker: String, url: String)

    fun pageEvent(page: AnalyticsPage)
}