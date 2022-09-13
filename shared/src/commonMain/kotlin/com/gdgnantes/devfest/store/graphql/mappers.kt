package com.gdgnantes.devfest.store.graphql

import com.gdgnantes.devfest.graphql.GetSessionQuery
import com.gdgnantes.devfest.graphql.fragment.SessionDetails
import com.gdgnantes.devfest.graphql.fragment.SpeakerDetails
import com.gdgnantes.devfest.graphql.fragment.VenueDetails
import com.gdgnantes.devfest.model.*

fun GetSessionQuery.Session.toSession(): Session = sessionDetails.toSession()

fun SessionDetails.toSession(): Session {
    return Session(
        id = id,
        abstract = description ?: "",
        category = null,
        language = null,
        complexity = null,
        openFeedbackFormId = "openFeedbackFormId",
        room = rooms.firstOrNull()?.toRoom(),
        scheduleSlot = ScheduleSlot(
            startDate = startInstant.toString(),
            endDate = endInstant.toString()
        ),
        speakers = speakers.map { it.toSpeaker() },
        title = title,
    )
}

fun SessionDetails.Room.toRoom(): Room {
    return with(roomDetails) {
        Room(
            id = id,
            name = name
        )
    }
}

fun SessionDetails.Speaker.toSpeaker(): Speaker {
    return with(speakerDetails) {
        Speaker(
            id = id,
            bio = bio,
            company = company,
            companyLogo = null,
            country = null,
            firstname = name,
            photoUrl = photoUrl,
            socials = socials.map { it.toSocial() },
            surname = null
        )
    }
}

fun SpeakerDetails.Social.toSocial(): SocialsItem {
    return with(socialDetails) {
        val type = when (name) {
            "twitter" -> SocialsType.TWITTER
            "github" -> SocialsType.GITHUB
            "linkedin" -> SocialsType.LINKEDIN
            "facebook" -> SocialsType.FACEBOOK
            "website" -> SocialsType.WEBSITE
            else -> SocialsType.WEBSITE
        }

        SocialsItem(
            type = type,
            link = link
        )
    }
}

fun VenueDetails.toVenue(): Venue {
    return Venue(
        address = address ?: "",
        descriptionEn = description,
        descriptionFr = descriptionFr,
        coordinates = coordinates,
        imageUrl = imageUrl,
        name = name
    )
}