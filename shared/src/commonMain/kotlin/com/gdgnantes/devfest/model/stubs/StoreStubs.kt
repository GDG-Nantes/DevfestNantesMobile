package com.gdgnantes.devfest.model.stubs


import com.gdgnantes.devfest.model.*
import com.gdgnantes.devfest.model.Agenda.Companion.DAY_ONE
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val MAX_PARTNERS = 50
const val MAX_ROOMS = 4
const val MAX_SESSIONS = 100
const val MAX_SPEAKERS = 100
const val MAX_SPEAKER_PER_SESSION = 5

fun buildSessionStub(): Session {
    return Session(
        id = Random.nextLong().toString(),
        abstract = loremIpsum,
        category = categoryStubs[Random.nextInt(categoryStubs.size)],
        language = SessionLanguage.values()[SessionLanguage.values().size - 1],
        complexity = Complexity.values()[Complexity.values().size - 1],
        openFeedbackFormId = "", //TODO Replaces with UUID implementation
        room = roomStubs[Random.nextInt(roomStubs.size)],
        scheduleSlot = buildScheduleSlotStub(),
        speakers = MutableList(Random.nextInt(1, MAX_SPEAKER_PER_SESSION)) {
            buildSpeakerStub()
        },
        title = "Amazing talk"
    )
}

fun buildScheduleSlotStub(): ScheduleSlot {
    val day = DAY_ONE
    val start = day.plus(Random.nextInt(9, 30).toDuration(DurationUnit.HOURS))
    val end = start.plus(1.toDuration(DurationUnit.HOURS))

    return ScheduleSlot(
        endDate = end.toString(),
        startDate = start.toString()
    )
}

fun buildSpeakerStub(): Speaker {
    return Speaker(
        id = "", //TODO Replace with UUID implementation
        bio = loremIpsum,
        company = "Company Inc",
        companyLogo = null,
        country = "France",
        firstname = "Foo",
        photoUrl = randomImageUrl,
        socials = socialItemStubs,
        surname = "Bar",
    )
}

fun buildVenueStub(): Venue {
    return Venue(
        name = "Cité des Congrès de Nantes",
        address = "5 rue de Valmy, 44000 Nantes",
        descriptionEn = "Located in the center of Nantes, the event takes place in the “Cité des Congrès” with more than 3000m² of conference rooms, hand’s on and networking space…",
        descriptionFr = "Située en plein cœur de ville, La Cité des Congrès de Nantes propose pour le DevFest Nantes plus de 3000m² de salles de conférences, codelabs et lieu de rencontre…",
        latitude = 47.21308725112951,
        longitude = -1.542622837466317,
        imageUrl = "https://devfest.gdgnantes.com/static/6328df241501c6e31393e568e5c68d7e/efc43/amphi.webp"
    )
}

fun buildPartnerStub(): Partner {
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