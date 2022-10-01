package com.gdgnantes.devfest.androidapp.services

import com.gdgnantes.devfest.analytics.AnalyticsEvent
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.analytics.AnalyticsParam
import com.gdgnantes.devfest.analytics.AnalyticsService
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import timber.log.Timber
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor(private val firebaseAnalytics: FirebaseAnalytics) :
    AnalyticsService {
    override fun eventBookmark(page: AnalyticsPage, sessionId: String, fav: Boolean) {
        if (fav) {
            firebaseAnalytics.logEvent(AnalyticsEvent.addToFavorite.value) {
                param(AnalyticsParam.sessionId.value, sessionId)
                param(AnalyticsParam.fromPage.value, page.value)
            }
        } else {
            firebaseAnalytics.logEvent(AnalyticsEvent.deleteToFavorite.value) {
                param(AnalyticsParam.sessionId.value, sessionId)
                param(AnalyticsParam.fromPage.value, page.value)
            }
        }
    }

    override fun eventFilter() {
        Timber.d("TODO: eventFilter")
    }

    override fun eventLinkCodeOfConductOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkCodeOfConductOpened.value) {}
    }

    override fun eventLinkDevFestWebsiteOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkDevFestWebsiteOpened.value) {}
    }

    override fun eventLinkFacebookOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkFacebookOpened.value) {}
    }

    override fun eventLinkLinkedinOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkLinkedinOpened.value) {}
    }

    override fun eventLinkLocalCommunitiesOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkLocalCommunitiesOpened.value) {}
    }

    override fun eventLinkPartnerOpened(partnerURL: String) {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkPartnerOpened.value) {
            param(AnalyticsParam.partnerURL.value, partnerURL)
        }
    }

    override fun eventLinkTwitterOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkTwitterOpened.value) {}
    }

    override fun eventLinkYoutubeOpened() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkYoutubeOpened.value) {}
    }

    override fun eventNavigationClicked() {
        firebaseAnalytics.logEvent(AnalyticsEvent.linkYoutubeOpened.value) {}
    }

    override fun eventSessionOpened(sessionId: String) {
        firebaseAnalytics.logEvent(AnalyticsEvent.sessionOpened.value) {
            param(AnalyticsParam.sessionId.value, sessionId)
        }
    }

    override fun eventSpeakerSocialLinkOpened(speaker: String, url: String) {
        firebaseAnalytics.logEvent(AnalyticsEvent.sessionOpened.value) {
            param(AnalyticsParam.speaker.value, speaker)
            param(AnalyticsParam.socialLink.value, url)
        }
    }

    override fun pageEvent(page: AnalyticsPage, className: String?) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, page.value)
            className?.run { param(FirebaseAnalytics.Param.SCREEN_CLASS, className) }
        }
    }

}
