package com.gdgnantes.devfest.analytics

enum class AnalyticsEvent {
    ADD_TO_BOOKMARKS,
    LINK_CODE_OF_CONDUCT_OPENED,
    LINK_DEVFEST_WEBSITE_OPENED,
    LINK_FACEBOOK_OPENED,
    LINK_GITHUB_OPENED,
    LINK_LINKEDIN_OPENED,
    LINK_LOCAL_COMMUNITIES_OPENED,
    LINK_PARTNER_OPENED,
    LINK_SUPPORT_OPENED,
    LINK_TWITTER_OPENED,
    FEEDBACK_CLICKED,
    REMOVE_FROM_BOOKMARKS,
    SESSION_OPENED,
    SPEAKER_OPENED,
    SPEAKER_SOCIAL_LINK_OPENED,
    LINK_YOUTUBE_OPENED,
    VENUE_FLOOR_PLAN_CLICKED,
    VENUE_NAVIGATION_CLICKED;

    override fun toString(): String {
        return name.lowercase()
    }
}
