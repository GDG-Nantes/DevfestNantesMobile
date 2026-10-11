package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.analytics.AnalyticsPage
import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.model.SocialType

/** [AnalyticsService] that ignores every event. DI-focused only; recording helpers belong to Phase 5. */
@Suppress("TooManyFunctions")
class FakeAnalyticsService : AnalyticsService {
    override fun eventBookmark(page: AnalyticsPage, sessionId: String, bookmarked: Boolean) = Unit

    override fun eventFilter() = Unit

    override fun eventLinkCodeOfConductOpened() = Unit

    override fun eventLinkDevFestWebsiteOpened() = Unit

    override fun eventLinkFacebookOpened() = Unit

    override fun eventLinkGithubOpened() = Unit

    override fun eventLinkLinkedinOpened() = Unit

    override fun eventLinkLocalCommunitiesOpened() = Unit

    override fun eventLinkPartnerOpened(partnerName: String) = Unit

    override fun eventLinkSupportOpened() = Unit

    override fun eventLinkTwitterOpened() = Unit

    override fun eventLinkYoutubeOpened() = Unit

    override fun eventFeedbackClicked(openFeedbackId: String) = Unit

    override fun eventSessionOpened(sessionId: String) = Unit

    override fun eventSpeakerOpened(speakerId: String) = Unit

    override fun eventSpeakerSocialLinkOpened(speakerId: String, type: SocialType) = Unit

    override fun eventVenueNavigationClicked() = Unit

    override fun eventVenueFloorPlanClicked() = Unit

    override fun pageEvent(page: AnalyticsPage, className: String?) = Unit
}
