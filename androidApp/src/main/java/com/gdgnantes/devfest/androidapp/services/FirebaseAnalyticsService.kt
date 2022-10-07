package com.gdgnantes.devfest.androidapp.services

import com.gdgnantes.devfest.analytics.AnalyticsEvent
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.analytics.AnalyticsParam
import com.gdgnantes.devfest.analytics.AnalyticsService
import com.gdgnantes.devfest.model.SocialType
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import timber.log.Timber
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor(private val firebaseAnalytics: FirebaseAnalytics) :
    AnalyticsService {
    override fun eventBookmark(page: AnalyticsPage, sessionId: String, bookmarked: Boolean) {
        if (bookmarked) {
            firebaseAnalytics.logEvent(AnalyticsEvent.ADD_TO_BOOKMARKS.toString()) {
                param(AnalyticsParam.SESSION_ID.toString(), sessionId)
                param(AnalyticsParam.FROM_PAGE.toString(), page.toString())
            }
        } else {
            firebaseAnalytics.logEvent(AnalyticsEvent.REMOVE_FROM_BOOKMARKS.toString()) {
                param(AnalyticsParam.SESSION_ID.toString(), sessionId)
                param(AnalyticsParam.FROM_PAGE.toString(), page.toString())
            }
        }
    }

    override fun eventFilter() {
        Timber.d("TODO: eventFilter")
    }

    override fun eventLinkCodeOfConductOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_CODE_OF_CONDUCT_OPENED.toString()) {}
    }

    override fun eventLinkDevFestWebsiteOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_DEVFEST_WEBSITE_OPENED.toString()) {}
    }

    override fun eventLinkFacebookOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_FACEBOOK_OPENED.toString()) {}
    }

    override fun eventLinkGithubOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_GITHUB_OPENED.toString()) {}
    }

    override fun eventLinkLinkedinOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_LINKEDIN_OPENED.toString()) {}
    }

    override fun eventLinkLocalCommunitiesOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_LOCAL_COMMUNITIES_OPENED.toString()) {}
    }

    override fun eventLinkPartnerOpened(partnerName: String) {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_PARTNER_OPENED.toString()) {
            param(AnalyticsParam.PARTNER_NAME.toString(), partnerName)
        }
    }

    override fun eventLinkSupportOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_SUPPORT_OPENED.toString()) {}
    }

    override fun eventLinkTwitterOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_TWITTER_OPENED.toString()) {}
    }

    override fun eventLinkYoutubeOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.LINK_YOUTUBE_OPENED.toString()) {}
    }

    override fun eventNavigationClicked() {
        firebaseAnalytics.logEvent(AnalyticsEvent.NAVIGATION_CLICKED.toString()) {}
    }

    override fun eventSessionOpened(sessionId: String) {
        firebaseAnalytics.logEvent(AnalyticsEvent.SESSION_OPENED.toString()) {
            param(AnalyticsParam.SESSION_ID.toString(), sessionId)
        }
    }

    override fun eventSpeakerSocialLinkOpened(speakerId: String, type: SocialType) {
        firebaseAnalytics.logEvent(AnalyticsEvent.SPEAKER_SOCIAL_LINK_OPENED.toString()) {
            param(AnalyticsParam.SPEAKER_ID.toString(), speakerId)
            param(AnalyticsParam.SOCIAL_TYPE.toString(), type.name)
        }
    }

    override fun pageEvent(page: AnalyticsPage, className: String?) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, page.toString())
            className?.run { param(FirebaseAnalytics.Param.SCREEN_CLASS, className) }
        }
    }

}
