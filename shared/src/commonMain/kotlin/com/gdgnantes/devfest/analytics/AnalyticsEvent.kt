package com.gdgnantes.devfest.analytics

enum class AnalyticsEvent {
    addToFavorite,
    deleteToFavorite,
    linkCodeOfConductOpened,
    linkDevFestWebsiteOpened,
    linkFacebookOpened,
    linkTwitterOpened,
    linkLinkedinOpened,
    linkLocalCommunitiesOpened,
    linkPartnerOpened,
    linkSponsorOpened,
    linkYoutubeOpened,
    navigationClicked,
    sessionOpened,
    speakerSocialLinkOpened;

    val value: String
        get() = name.lowercase()
}
