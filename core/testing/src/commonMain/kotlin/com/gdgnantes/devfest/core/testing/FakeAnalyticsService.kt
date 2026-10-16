package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.analytics.AnalyticsPage
import com.gdgnantes.devfest.core.analytics.AnalyticsService
import com.gdgnantes.devfest.core.model.SocialType

/** One recorded call to [AnalyticsService.eventBookmark]. */
data class BookmarkEvent(
    val page: AnalyticsPage,
    val sessionId: String,
    val bookmarked: Boolean
)

/**
 * [AnalyticsService] that ignores every event except bookmarks: it records bookmark events
 * (Phase 5) in [bookmarkEvents]. Every other method is a no-op.
 */
@Suppress("TooManyFunctions")
class FakeAnalyticsService : AnalyticsService {
    private val recordedBookmarkEvents = mutableListOf<BookmarkEvent>()

    /** Bookmark events in the order they were reported. */
    val bookmarkEvents: List<BookmarkEvent>
        get() = recordedBookmarkEvents.toList()

    override fun eventBookmark(page: AnalyticsPage, sessionId: String, bookmarked: Boolean) {
        recordedBookmarkEvents += BookmarkEvent(page, sessionId, bookmarked)
    }

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
