package com.gdgnantes.devfest.stubs

import com.gdgnantes.devfest.data.*
import kotlin.random.Random

const val MAX_SESSIONS_PER_DAY = 20
const val MAX_SPEAKER_PER_SESSION = 5

fun createAgendaDayStub(): AgendaDay {
    return AgendaDay(
        id = Random.nextInt(),
        sessions = MutableList(Random.nextInt(1, MAX_SESSIONS_PER_DAY)) {
            createAgendaSessionStub()
        }
    )
}

fun createAgendaSessionStub(): AgendaSession {
    return AgendaSession(
        id = Random.nextInt(),
        abstract = loremIpsum,
        level = AgendaSessionLevel.values()[AgendaSessionLevel.values().size - 1],
        category = AgendaSessionCategory.values()[AgendaSessionCategory.values().size - 1],
        language = AgendaSessionLanguage.values()[AgendaSessionLanguage.values().size - 1],
        speakers = MutableList(Random.nextInt(1, MAX_SPEAKER_PER_SESSION)) {
            createSpeakerStub()
        },
        openFeedbackFormId = Random.nextInt(),
        room = createRoomStub()
    )
}

fun createSpeakerStub(): Speaker {
    return Speaker(
        id = Random.nextInt(),
        firstname = "Foo",
        surname = "Bar",
        photoUrl = "https://fakeface.rest/thumb/view",
        biography = loremIpsum,
        twitter = "devfestnantes",
        github = "GDG-Nantes",
        city = "Nantes"
    )
}

fun createRoomStub(): Room {
    return Room(
        id = Random.nextInt(),
        name = "Foo"
    )
}

const val loremIpsum =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."