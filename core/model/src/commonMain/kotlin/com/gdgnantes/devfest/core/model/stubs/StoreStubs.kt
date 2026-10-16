package com.gdgnantes.devfest.core.model.stubs

import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.Partner
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.Venue

/**
 * Deterministic entry points for Compose previews. The data lives in [SessionFixtures],
 * [SpeakerFixtures] and [PartnerFixtures].
 */
fun buildSessionStub(): Session = SessionFixtures.composeDeepDive

fun buildScheduleSlotStub(): ScheduleSlot = SessionFixtures.composeDeepDive.scheduleSlot

fun buildSpeakerStub(): Speaker = SpeakerFixtures.alice

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

fun buildPartnerStub(): Partner = PartnerFixtures.byCategory.getValue(PartnerCategory.PLATINIUM).first()

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
