@file:Suppress("TooManyFunctions")

package com.gdgnantes.devfest.store.graphql

import com.gdgnantes.devfest.domain.sortIndex
import com.gdgnantes.devfest.graphql.GetPartnerGroupsQuery
import com.gdgnantes.devfest.graphql.GetSessionQuery
import com.gdgnantes.devfest.graphql.GetVenueQuery
import com.gdgnantes.devfest.graphql.fragment.RoomDetails
import com.gdgnantes.devfest.graphql.fragment.SessionDetails
import com.gdgnantes.devfest.graphql.fragment.SpeakerDetails
import com.gdgnantes.devfest.model.Category
import com.gdgnantes.devfest.model.Complexity
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.PartnerCategory
import com.gdgnantes.devfest.model.Room
import com.gdgnantes.devfest.model.ScheduleSlot
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionLanguage
import com.gdgnantes.devfest.model.SessionType
import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.SocialType
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.Venue

fun GetPartnerGroupsQuery.PartnerGroup.toPartnersGroup(): Pair<PartnerCategory, List<Partner>> {
    val partnerCategory =
        when (title.lowercase()) {
            "platinium" -> PartnerCategory.PLATINIUM
            "gold" -> PartnerCategory.GOLD
            "virtual" -> PartnerCategory.VIRTUAL
            else -> PartnerCategory.PARTNERS
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
        category =
        tag?.run {
            Category(
                id = lowercase(),
                label = this
            )
        },
        language = language?.toSessionLanguage(),
        complexity = complexity?.toComplexity(),
        openFeedbackFormId = feedbackId ?: title.lowercase().replace(" ", ""),
        room = rooms.firstOrNull()?.roomDetails?.toRoom(),
        scheduleSlot =
        ScheduleSlot(
            startDate = startInstant.toString(),
            endDate = endInstant.toString()
        ),
        speakers = speakers.map { it.speakerDetails.toSpeaker() },
        title = title,
        type = type.toSessionType()
    )
}

fun RoomDetails.toRoom(): Room {
    return Room(
        id = id,
        name = name,
        sortIndex = sortIndex
    )
}

fun SpeakerDetails.toSpeaker(): Speaker {
    return Speaker(
        id = id,
        bio = bio,
        company = company,
        companyLogoUrl = companyLogoUrl,
        city = city,
        name = name,
        photoUrl = photoUrl,
        socials = socials.map { it.toSocial() }
    )
}

fun SpeakerDetails.Social.toSocial(): SocialItem {
    return with(socialDetails) {
        SocialItem.Builder()
            .setType(name.toSocialType())
            .setLink(link)
            .build()
    }
}

fun GetVenueQuery.Venue.toVenue(): Venue {
    return Venue(
        address = address ?: "",
        description = description,
        floorPlanUrl = floorPlanUrl,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        name = name
    )
}

private fun String.toSessionType(): SessionType? {
    return when (this.lowercase()) {
        "break" -> SessionType.BREAK
        "conference" -> SessionType.CONFERENCE
        "codelab" -> SessionType.CODELAB
        "keynote" -> SessionType.KEYNOTE
        "lunch" -> SessionType.LUNCH
        "opening" -> SessionType.OPENING
        "quickie" -> SessionType.QUICKIE
        else -> null
    }
}

private fun String.toSocialType(): SocialType {
    return when (this.lowercase()) {
        "twitter" -> SocialType.TWITTER
        "github" -> SocialType.GITHUB
        "linkedin" -> SocialType.LINKEDIN
        "facebook" -> SocialType.FACEBOOK
        "website" -> SocialType.WEBSITE
        else -> SocialType.WEBSITE
    }
}

private fun String.toSessionLanguage(): SessionLanguage? {
    return when (this) {
        "fr-FR" -> SessionLanguage.FRENCH
        "en-US" -> SessionLanguage.ENGLISH
        else -> null
    }
}

private fun String.toComplexity(): Complexity? {
    return when (this.lowercase()) {
        "beginner" -> Complexity.BEGINNER
        "intermediate" -> Complexity.INTERMEDIATE
        "advanced" -> Complexity.ADVANCED
        else -> null
    }
}
