package com.gdgnantes.devfest.kotlinmultiplatformsharedmodule.stubs


import com.gdgnantes.devfest.store.model.*
import kotlin.random.Random

const val MAX_PARTNERS = 50
const val MAX_ROOMS = 4
const val MAX_SESSIONS = 100
const val MAX_SPEAKERS = 100
const val MAX_SPEAKER_PER_SESSION = 5

fun buildSessionStub(): Session {
    return Session(
        id = "", //TODO Replaces with UUID implementation
        abstract = loremIpsum,
        category = SessionCategory.values()[SessionCategory.values().size - 1],
        language = SessionLanguage.values()[SessionLanguage.values().size - 1],
        level = SessionLevel.values()[SessionLevel.values().size - 1],
        openFeedbackFormId = "", //TODO Replaces with UUID implementation
        room = buildRoomStub(),
        scheduleSlot = buildScheduleSlotStub(),
        speakers = MutableList(Random.nextInt(1, MAX_SPEAKER_PER_SESSION)) {
            buildSpeakerStub()
        },
        title = "Amazing talk"
    )
}

fun buildScheduleSlotStub() = ScheduleSlot(
    endDate = "2022-10-20T11:15:02+00:00",
    startDate = "2022-10-20T10:15:02+00:00"
)

fun buildSpeakerStub(): Speaker {
    return Speaker(
        id = "", //TODO Replace with UUID implementation
        bio = loremIpsum,
        company = "Company Inc",
        companyLogo = null,
        country = "France",
        firstname = "Foo",
        photoUrl = randomImageUrl,
        socials = emptyList(),
        surname = "Bar",
    )
}

fun buildRoomStub(): Room {
    return Room(
        id = "", //TODO Replace with UUID implementation
        name = "Foo"
    )
}

fun buildVenueStub(): Venue {
    return Venue(
        address = "1 rue de la ville 01000 Ville",
        descriptionEn = loremIpsum,
        descriptionFr = loremIpsum
    )
}

fun buildPartner(): Partner {
    return Partner(
        name = "Partner ${Random.nextInt()}",
        imageUrl = randomImageUrl,
        link = "https://kotlinlang.org/docs/multiplatform.html",
        description = loremIpsum,
        order = Random.nextInt()
    )
}

const val loremIpsum =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

const val randomImageUrl = "https://fakeface.rest/thumb/view"