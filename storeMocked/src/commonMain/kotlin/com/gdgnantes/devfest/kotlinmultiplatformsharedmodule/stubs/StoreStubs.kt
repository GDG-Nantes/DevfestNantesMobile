package com.gdgnantes.devfest.kotlinmultiplatformsharedmodule.stubs


import com.gdgnantes.devfest.store.model.*
import kotlin.random.Random

const val MAX_SESSIONS = 100
const val MAX_SPEAKERS = 100
const val MAX_ROOMS = 4
const val MAX_SPEAKER_PER_SESSION = 5

fun createSessionStub(): Session {
    return Session(
        id = "", //TODO Replaces with UUID implementation
        abstract = loremIpsum,
        category = SessionCategory.values()[SessionCategory.values().size - 1],
        language = SessionLanguage.values()[SessionLanguage.values().size - 1],
        level = SessionLevel.values()[SessionLevel.values().size - 1],
        openFeedbackFormId = "", //TODO Replaces with UUID implementation
        room = createRoomStub(),
        scheduleSlot = createScheduleSlotStub(),
        speakers = MutableList(Random.nextInt(1, MAX_SPEAKER_PER_SESSION)) {
            createSpeakerStub()
        },
        title = "Amazing talk"
    )
}

fun createScheduleSlotStub() = ScheduleSlot(
    endDate = "2022-10-20T11:15:02+00:00",
    startDate = "2022-10-20T10:15:02+00:00"
)

fun createSpeakerStub(): Speaker {
    return Speaker(
        id = "", //TODO Replace with UUID implementation
        bio = loremIpsum,
        company = "Company Inc",
        companyLogo = null,
        country = "France",
        firstname = "Foo",
        photoUrl = "https://fakeface.rest/thumb/view",
        socials = emptyList(),
        surname = "Bar",
    )
}

fun createRoomStub(): Room {
    return Room(
        id = "", //TODO Replace with UUID implementation
        name = "Foo"
    )
}

const val loremIpsum =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."