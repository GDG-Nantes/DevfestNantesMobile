package com.gdgnantes.devfest.analytics

interface AnalyticsService {

    /*
    Notes:
    * Enums should be capitalized
    * eventBookmark param should be bookmarked instead of fav
    * eventSpeakerSocialLinkOpened should specify the social network instead of the url and speakerId instead of speaker
    * I added className to the pageEvent. Why not using it in iOS ?
     */

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

    fun pageEvent(page: AnalyticsPage, className: String? = null)
}