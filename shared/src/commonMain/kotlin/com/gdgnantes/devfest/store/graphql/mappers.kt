package com.gdgnantes.devfest.store.graphql

import com.gdgnantes.devfest.graphql.GetPartnerGroupsQuery
import com.gdgnantes.devfest.graphql.GetSessionQuery
import com.gdgnantes.devfest.graphql.GetVenueQuery
import com.gdgnantes.devfest.graphql.fragment.RoomDetails
import com.gdgnantes.devfest.graphql.fragment.SessionDetails
import com.gdgnantes.devfest.graphql.fragment.SpeakerDetails
import com.gdgnantes.devfest.model.*

fun GetPartnerGroupsQuery.PartnerGroup.toPartnersGroup(): Pair<PartnerCategory, List<Partner>> {
    val partnerCategory = when (title.lowercase()) {
        "platinium" -> PartnerCategory.PLATINIUM
        "gold" -> PartnerCategory.GOLD
        "virtual" -> PartnerCategory.VIRTUAL
        else -> PartnerCategory.VIRTUAL
    }
    return partnerCategory to partners.map { it.toPartner() }
}

fun GetPartnerGroupsQuery.Partner.toPartner(): Partner {
    return Partner(
        name = name,
        logoUrl = logoUrl,
        url = url
    )
}

fun GetSessionQuery.Session.toSession(): Session = sessionDetails.toSession()

fun SessionDetails.toSession(): Session {
    val tag = if (tags.isNotEmpty()) tags.joinToString(", ") else null
    return Session(
        id = id,
        abstract = description ?: "",
        category = tag?.run {
            Category(
                id = lowercase(),
                label = this
            )
        },
        language = null,
        complexity = null,
        openFeedbackFormId = feedbackId,
        room = rooms.firstOrNull()?.roomDetails?.toRoom(),
        scheduleSlot = ScheduleSlot(
            startDate = startInstant.toString(),
            endDate = endInstant.toString()
        ),
        speakers = speakers.map { it.speakerDetails.toSpeaker() },
        title = title,
    )
}

fun RoomDetails.toRoom(): Room {
    return Room(
        id = id,
        name = name
    )
}

fun SpeakerDetails.toSpeaker(): Speaker {
    return Speaker(
        id = id,
        bio = bio,
        company = company,
        companyLogo = null,
        country = null,
        name = name,
        photoUrl = photoUrl,
        socials = socials.map { it.toSocial() }
    )
}

fun SpeakerDetails.Social.toSocial(): SocialItem {
    return with(socialDetails) {
        val type = when (name.lowercase()) {
            "twitter" -> SocialType.TWITTER
            "github" -> SocialType.GITHUB
            "linkedin" -> SocialType.LINKEDIN
            "facebook" -> SocialType.FACEBOOK
            "website" -> SocialType.WEBSITE
            else -> SocialType.WEBSITE
        }

        SocialItem.Builder()
            .setType(type)
            .setLink(link)
            .build()
    }
}

fun GetVenueQuery.Venue.toVenue(): Venue {
    return Venue(
        address = address ?: "",
        description = description,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        name = name
    )
}