package com.gdgnantes.devfest.model.stubs

import com.gdgnantes.devfest.model.Agenda.Companion.DAY_ONE
import com.gdgnantes.devfest.model.Complexity
import com.gdgnantes.devfest.model.ContentLanguage
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.ScheduleSlot
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionLanguage
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.Venue
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val MAX_PARTNERS = 10
const val MAX_ROOMS = 4
const val MAX_SESSIONS = 100
const val MAX_SPEAKERS = 100
const val MAX_SPEAKER_PER_SESSION = 5

fun buildSessionStub(): Session {
    return Session(
        id = Random.nextLong().toString(),
        abstract = LOREM_IPSUM,
        category = categoryStubs[Random.nextInt(categoryStubs.size)],
        language = SessionLanguage.values()[SessionLanguage.values().size - 1],
        complexity = Complexity.values()[Complexity.values().size - 1],
        openFeedbackFormId = "", // TODO Replaces with UUID implementation
        room = roomStubs[Random.nextInt(roomStubs.size)],
        scheduleSlot = buildScheduleSlotStub(),
        speakers =
        MutableList(
            Random.nextInt(1, MAX_SPEAKER_PER_SESSION)
        ) {
            buildSpeakerStub()
        },
        title = "Amazing talk"
    )
}

const val STUB_STARTING_HOUR = 9
const val STUB_ENDING_HOUR = 30
fun buildScheduleSlotStub(): ScheduleSlot {
    val day = DAY_ONE
    val start =
        day.plus(
            Random.nextInt(STUB_STARTING_HOUR, STUB_ENDING_HOUR)
                .toDuration(DurationUnit.HOURS)
        )
    val end = start.plus(1.toDuration(DurationUnit.HOURS))

    return ScheduleSlot(
        endDate = end.toString(),
        startDate = start.toString()
    )
}

fun buildSpeakerStub(): Speaker {
    return Speaker(
        id = "", // TODO Replace with UUID implementation
        bio = LOREM_IPSUM,
        company = "Company Inc",
        companyLogoUrl = null,
        city = "Nantes, France",
        name = "Foo Bar",
        photoUrl = RANDOM_IMAGE_URL,
        socials = SOCIAL_ITEMS_STUBS,
    )
}

fun buildVenueStub(language: ContentLanguage): Venue {
    return Venue(
        name = "Cité des Congrès de Nantes",
        address = "5 rue de Valmy, 44000 Nantes",
        description =
        if (language == ContentLanguage.ENGLISH) {
            "Located in the center of Nantes," +
                    " the event takes place in the “Cité des Congrès”" +
                    " with more than 3000m² of conference rooms, hand’s" +
                    " on and networking space…"
        } else {
            "Située en plein cœur de ville, La Cité des Congrès" +
                    " de Nantes propose pour le DevFest Nantes plus" +
                    " de 3000m² de salles de conférences, codelabs et" +
                    " lieu de rencontre…"
        },
        floorPlanUrl = "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/src/images/plan-cite-blanc.png",
        latitude = 47.21308725112951,
        longitude = -1.542622837466317,
        imageUrl = "https://devfest.gdgnantes.com/static/6328df241501c6e31393e568e5c68d7e/efc43/amphi.webp"
    )
}

fun buildPartnerStub(): Partner {
    return Partner(
        name = "Partner ${Random.nextInt()}",
        logoUrl = RANDOM_IMAGE_URL,
        url = "https://kotlinlang.org/docs/multiplatform.html"
    )
}

const val LOREM_IPSUM =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit," +
            " sed do eiusmod tempor incididunt ut labore et" +
            " dolore magna aliqua. Ut enim ad minim veniam," +
            " quis nostrud exercitation ullamco laboris nisi" +
            " ut aliquip ex ea commodo consequat. Duis aute" +
            " irure dolor in reprehenderit in voluptate velit" +
            " esse cillum dolore eu fugiat nulla pariatur." +
            " Excepteur sint occaecat cupidatat non proident," +
            " sunt in culpa qui officia deserunt mollit anim" +
            " id est laborum."

const val RANDOM_IMAGE_URL = "https://fakeface.rest/thumb/view"
