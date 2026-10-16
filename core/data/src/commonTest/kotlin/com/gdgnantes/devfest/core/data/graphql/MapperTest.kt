package com.gdgnantes.devfest.core.data.graphql

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.json.jsonReader
import com.apollographql.apollo.api.parseResponse
import com.gdgnantes.devfest.core.model.Category
import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.SocialType
import com.gdgnantes.devfest.core.network.graphql.GetPartnerGroupsQuery
import com.gdgnantes.devfest.core.network.graphql.GetRoomsQuery
import com.gdgnantes.devfest.core.network.graphql.GetSessionQuery
import com.gdgnantes.devfest.core.network.graphql.GetSessionsQuery
import com.gdgnantes.devfest.core.network.graphql.GetSpeakersQuery
import com.gdgnantes.devfest.core.network.graphql.GetVenueQuery
import com.gdgnantes.devfest.core.network.graphql.builder.Data
import com.gdgnantes.devfest.core.network.graphql.builder.GraphQLSessionBuilder
import com.gdgnantes.devfest.core.network.graphql.builder.buildGraphQLPartner
import com.gdgnantes.devfest.core.network.graphql.builder.buildGraphQLRoom
import com.gdgnantes.devfest.core.network.graphql.builder.buildGraphQLSession
import com.gdgnantes.devfest.core.network.graphql.builder.buildGraphQLSpeaker
import com.gdgnantes.devfest.core.network.graphql.builder.buildGraphQLVenue
import com.gdgnantes.devfest.core.network.graphql.builder.buildPartnerGroup
import com.gdgnantes.devfest.core.network.graphql.builder.buildSessionConnection
import com.gdgnantes.devfest.core.network.graphql.builder.buildSocial
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Parses a recorded response body the way the Apollo client does, without any network. */
internal fun <D : Operation.Data> Operation<D>.parseRecorded(json: String): D =
    parseResponse(Buffer().writeUtf8(json).jsonReader()).dataOrThrow()

/**
 * Mapper tests (D-13). The happy path comes from the recorded responses, with expected values
 * written as literals taken from the recording. Edge cases that the recording does not contain
 * come from Apollo data builders.
 */
class MapperTest {

    // region recorded responses

    @Test
    fun recorded_sessions_map_every_field_of_the_keynote() {
        val session = recordedSessions().first { it.id == "keynotedouverture" }

        assertEquals("Opening Keynote - Advice Well Taken", session.title)
        assertTrue(session.abstract.startsWith("En 2023, Ilina a passé une année"))
        assertEquals(SessionLanguage.FRENCH, session.language)
        assertEquals(Complexity.BEGINNER, session.complexity)
        assertEquals(SessionType.CONFERENCE, session.type)
        assertEquals(Category(id = "💡 discovery", label = "💡 Discovery"), session.category)
        assertEquals("keynotedouverture", session.openFeedbackFormId)
        assertEquals("2025-10-16T09:00:00+02:00", session.scheduleSlot.startDate)
        assertEquals("2025-10-16T09:50:00+02:00", session.scheduleSlot.endDate)
        assertEquals("Jules Verne", session.room?.id)
        assertEquals(0, session.room?.sortIndex)
        assertEquals(listOf("dasha_ilina"), session.speakers.map { it.id })
    }

    @Test
    fun recorded_feedback_id_wins_over_the_session_id() {
        val session = recordedSessions().first { it.id == "thefutureofroutingwiththenavigationapi" }

        assertEquals("lefuturduroutingaveclanavigationapi", session.openFeedbackFormId)
        assertEquals(SessionLanguage.ENGLISH, session.language)
        assertEquals(Complexity.INTERMEDIATE, session.complexity)
        assertEquals("Tour Bretagne", session.room?.id)
        // "Tour Bretagne" is not in RoomSortIndex ("Tour de Bretagne" is), so it sorts last.
        assertEquals(Int.MAX_VALUE, session.room?.sortIndex)
    }

    @Test
    fun recorded_codelab_keeps_the_speaker_order() {
        val session = recordedSessions().first { it.id == "laguerredespromptsattaquesdefensesauroyaumedesllm" }

        assertEquals(SessionType.CODELAB, session.type)
        assertEquals(
            listOf("benjamin_bernard", "francois_nollen", "pierre_therrode", "xavier_charef"),
            session.speakers.map { it.id }
        )
        assertEquals(Category(id = "🤖 bigdata & ai", label = "🤖 BigData & AI"), session.category)
    }

    @Test
    fun recorded_lunch_has_no_room_speakers_category_or_complexity() {
        val session = recordedSessions().first { it.id == "day-1-lunch" }

        assertEquals(SessionType.LUNCH, session.type)
        assertNull(session.room)
        assertEquals(emptyList(), session.speakers)
        assertNull(session.category)
        assertNull(session.complexity)
        assertEquals("day-1-lunch", session.openFeedbackFormId)
        assertEquals("2025-10-16T12:00:00+02:00", session.scheduleSlot.startDate)
    }

    @Test
    fun recorded_single_session_maps_like_the_list_entry() {
        val fromSingle = GetSessionQuery("thefutureofroutingwiththenavigationapi")
            .parseRecorded(RecordedResponses.SESSION)
            .session.toSession()
        val fromList = recordedSessions().first { it.id == "thefutureofroutingwiththenavigationapi" }

        assertEquals(fromList, fromSingle)
        assertEquals("The future of routing with the Navigation API", fromSingle.title)
    }

    @Test
    fun recorded_speakers_map_profile_and_socials() {
        val speakers = GetSpeakersQuery().parseRecorded(RecordedResponses.SPEAKERS).speakers
            .map { it.speakerDetails.toSpeaker() }
        val eduardo = speakers.first { it.id == "eduardo_san_martin_morote" }

        // xavier_charef speaks in a recorded session but is missing here: the deprecated
        // `speakers` field only returns the first 100 of 110 speakers (deferred-items.md).
        assertEquals(7, speakers.size)
        assertTrue(speakers.none { it.id == "xavier_charef" })
        assertEquals("Eduardo San Martin Morote", eduardo.name)
        assertTrue(eduardo.bio.orEmpty().startsWith("Eduardo est développeur frontend freelance"))
        assertEquals("Freelance", eduardo.company)
        assertNull(eduardo.companyLogoUrl)
        assertEquals("Paris, France", eduardo.city)
        assertEquals(
            "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/" +
                "eduardo_san_martin_morote.jpg",
            eduardo.photoUrl
        )
        assertEquals(
            listOf(SocialType.TWITTER, SocialType.LINKEDIN),
            eduardo.socials?.map { it.type }
        )
        assertEquals(
            listOf("https://twitter.com/posva", "https://www.linkedin.com/posva"),
            eduardo.socials?.map { it.link }
        )
    }

    @Test
    fun recorded_unknown_social_name_and_empty_url_map_to_a_website_item() {
        val speakers = GetSpeakersQuery().parseRecorded(RecordedResponses.SPEAKERS).speakers
            .map { it.speakerDetails.toSpeaker() }
        val patrice = speakers.first { it.id == "patrice_de_saint_steban" }

        assertEquals(
            listOf(SocialType.GITHUB, SocialType.WEBSITE, SocialType.LINKEDIN),
            patrice.socials?.map { it.type }
        )
        assertEquals("https://github.com/patou", patrice.socials?.first()?.link)
        assertEquals("", patrice.socials?.get(1)?.link)
    }

    @Test
    fun recorded_speaker_with_a_company_logo_keeps_it() {
        val speakers = GetSpeakersQuery().parseRecorded(RecordedResponses.SPEAKERS).speakers
            .map { it.speakerDetails.toSpeaker() }
        val francois = speakers.first { it.id == "francois_nollen" }

        assertEquals("SNCF Connect & Tech", francois.company)
        assertEquals(
            "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srcsncf_connect___tech.png",
            francois.companyLogoUrl
        )
        assertEquals(
            listOf("https://github.com/francoisno", "https://twitter.com/FrancoisN0"),
            francois.socials?.take(2)?.map { it.link }
        )
    }

    @Test
    fun recorded_partner_groups_map_to_categories_in_order() {
        val groups = GetPartnerGroupsQuery().parseRecorded(RecordedResponses.PARTNER_GROUPS).partnerGroups
            .map { it.toPartnersGroup() }

        assertEquals(
            listOf(
                PartnerCategory.PXL,
                PartnerCategory.PLATINIUM,
                PartnerCategory.GOLD,
                PartnerCategory.VIRTUAL,
                PartnerCategory.VELOTYPE
            ),
            groups.map { it.first }
        )
        val dataiku = groups.first().second.single()
        assertEquals("Dataiku", dataiku.name)
        assertEquals("https://www.dataiku.com/", dataiku.url)
        assertEquals(
            "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main//src/images/partners/dataiku.png",
            dataiku.logoUrl
        )
    }

    @Test
    fun recorded_venue_maps_in_both_languages() {
        val en = GetVenueQuery("main", "en").parseRecorded(RecordedResponses.VENUE_EN).venue.toVenue()
        val fr = GetVenueQuery("main", "fr").parseRecorded(RecordedResponses.VENUE_FR).venue.toVenue()

        assertEquals("Cité des Congrès de Nantes", en.name)
        assertEquals("5 rue de Valmy, 44000 Nantes", en.address)
        assertEquals(47.21308725112951, en.latitude)
        assertEquals(-1.542622837466317, en.longitude)
        assertEquals(
            "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/plan-cite-transparent.png",
            en.floorPlanUrl
        )
        assertEquals(
            "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/home/album/equipe-amphi.jpg",
            en.imageUrl
        )
        assertTrue(en.description.startsWith("Located in the center of Nantes"))
        assertTrue(fr.description.startsWith("Située en plein cœur de ville"))
        assertEquals(en.copy(description = fr.description), fr)
    }

    @Test
    fun recorded_rooms_get_their_sort_index() {
        val rooms = GetRoomsQuery().parseRecorded(RecordedResponses.ROOMS).rooms.map { it.roomDetails.toRoom() }

        assertEquals(
            mapOf(
                "Belem" to 2,
                "Hangar" to 5,
                "Jules Verne" to 0,
                "L'Atelier" to 6,
                "Les Machines" to 4,
                "Titan" to 1,
                "Tour Bretagne" to Int.MAX_VALUE,
                "Unconf" to Int.MAX_VALUE
            ),
            rooms.associate { it.id to it.sortIndex }
        )
    }

    // endregion

    // region data builders

    @Test
    fun language_null_or_unknown_maps_to_null() {
        assertNull(buildSession { language = null }.language)
        assertNull(buildSession { language = "de-DE" }.language)
        assertEquals(SessionLanguage.ENGLISH, buildSession { language = "en-US" }.language)
    }

    @Test
    fun complexity_unknown_maps_to_null_and_is_case_insensitive() {
        assertNull(buildSession { complexity = "expert" }.complexity)
        assertNull(buildSession { complexity = null }.complexity)
        assertEquals(Complexity.ADVANCED, buildSession { complexity = "ADVANCED" }.complexity)
    }

    @Test
    fun type_unknown_maps_to_null_and_known_types_are_case_insensitive() {
        assertNull(buildSession { type = "party" }.type)
        assertEquals(SessionType.KEYNOTE, buildSession { type = "Keynote" }.type)
        assertEquals(SessionType.QUICKIE, buildSession { type = "quickie" }.type)
        assertEquals(SessionType.OPENING, buildSession { type = "opening" }.type)
        assertEquals(SessionType.BREAK, buildSession { type = "break" }.type)
    }

    @Test
    fun session_without_rooms_has_no_room() {
        assertNull(buildSession { rooms = emptyList() }.room)
    }

    @Test
    fun session_takes_the_first_room_when_there_are_several() {
        val session = buildSession {
            rooms = listOf(
                buildGraphQLRoom {
                    id = "Titan"
                    name = "Titan"
                },
                buildGraphQLRoom {
                    id = "Belem"
                    name = "Belem"
                }
            )
        }

        assertEquals("Titan", session.room?.id)
    }

    @Test
    fun session_without_speakers_has_an_empty_speaker_list() {
        assertEquals(emptyList(), buildSession { speakers = emptyList() }.speakers)
    }

    @Test
    fun tags_are_joined_into_one_category_and_no_tags_means_no_category() {
        assertNull(buildSession { tags = emptyList() }.category)
        assertEquals(
            Category(id = "web, cloud", label = "Web, Cloud"),
            buildSession { tags = listOf("Web", "Cloud") }.category
        )
    }

    @Test
    fun missing_feedback_id_falls_back_to_the_session_id() {
        val session = buildSession {
            id = "session-x"
            feedbackId = null
        }

        assertEquals("session-x", session.openFeedbackFormId)
    }

    @Test
    fun missing_description_maps_to_an_empty_abstract() {
        assertEquals("", buildSession { description = null }.abstract)
    }

    // Pins a fixed +02:00 offset, wrong outside Paris summer time (deferred item 18 in deferred-items.md).
    @Test
    fun start_dates_get_the_paris_offset_appended() {
        val minutes = buildSession {
            startsAt = "2025-10-16T09:00"
            endsAt = "2025-10-16T09:50"
        }
        val seconds = buildSession {
            startsAt = "2025-10-16T09:00:00"
            endsAt = "2025-10-16T09:50:00"
        }
        val utc = buildSession {
            startsAt = "2025-10-16T07:00:00Z"
            endsAt = "2025-10-16T07:50:00Z"
        }

        assertEquals("2025-10-16T09:00:00+02:00", minutes.scheduleSlot.startDate)
        assertEquals("2025-10-16T09:50:00+02:00", minutes.scheduleSlot.endDate)
        assertEquals("2025-10-16T09:00:00+02:00", seconds.scheduleSlot.startDate)
        assertEquals("2025-10-16T09:50:00+02:00", seconds.scheduleSlot.endDate)
        assertEquals("2025-10-16T07:00:00Z", utc.scheduleSlot.startDate)
        assertEquals("2025-10-16T07:50:00Z", utc.scheduleSlot.endDate)
    }

    @Test
    fun social_named_mastodon_maps_to_a_website_item() {
        val session = buildSession {
            speakers = listOf(
                buildGraphQLSpeaker {
                    id = "speaker-x"
                    name = "Speaker X"
                    bio = null
                    company = null
                    companyLogoUrl = null
                    city = null
                    photoUrl = null
                    socials = listOf(
                        buildSocial {
                            name = "mastodon"
                            url = "https://mastodon.example/@x"
                            icon = null
                        }
                    )
                }
            )
        }

        val social = session.speakers.single().socials?.single()
        assertEquals(SocialType.WEBSITE, social?.type)
        assertEquals("https://mastodon.example/@x", social?.link)
    }

    @Test
    fun unknown_room_id_sorts_last() {
        val session = buildSession {
            rooms = listOf(
                buildGraphQLRoom {
                    id = "Mystery Room"
                    name = "Mystery Room"
                }
            )
        }

        assertEquals(Int.MAX_VALUE, session.room?.sortIndex)
        assertEquals("Mystery Room", session.room?.name)
    }

    @Test
    fun partner_group_title_is_case_insensitive_and_unknown_titles_map_to_partners() {
        val data = GetPartnerGroupsQuery.Data {
            partnerGroups = listOf("GOLD", "Friends", "virtual").map { groupTitle ->
                buildPartnerGroup {
                    title = groupTitle
                    partners = listOf(
                        buildGraphQLPartner {
                            name = "Acme"
                            logoUrl = "https://example.com/acme.png"
                            url = "https://example.com"
                        }
                    )
                }
            }
        }

        assertEquals(
            listOf(PartnerCategory.GOLD, PartnerCategory.PARTNERS, PartnerCategory.VIRTUAL),
            data.partnerGroups.map { it.toPartnersGroup().first }
        )
    }

    @Test
    fun venue_without_address_maps_to_an_empty_address() {
        val data = GetVenueQuery.Data {
            venue = buildGraphQLVenue {
                name = "Somewhere"
                address = null
                description = "A venue"
                latitude = 1.5
                longitude = 2.5
                imageUrl = null
                floorPlanUrl = null
            }
        }

        val venue = data.venue.toVenue()
        assertEquals("", venue.address)
        assertEquals("Somewhere", venue.name)
        assertEquals("A venue", venue.description)
        assertEquals(1.5, venue.latitude)
        assertEquals(2.5, venue.longitude)
        assertNull(venue.imageUrl)
        assertNull(venue.floorPlanUrl)
    }

    // endregion

    private fun recordedSessions(): List<Session> =
        GetSessionsQuery().parseRecorded(RecordedResponses.SESSIONS).sessions.nodes
            .map { it.sessionDetails.toSession() }
            .also { assertNotNull(it.firstOrNull()) }

    /** One session built from data builders: valid defaults, overridden per test. */
    private fun buildSession(override: GraphQLSessionBuilder.() -> Unit = {}): Session {
        val data = GetSessionsQuery.Data {
            sessions = buildSessionConnection {
                nodes = listOf(
                    buildGraphQLSession {
                        id = "session-1"
                        title = "A session"
                        description = "About the session"
                        language = "fr-FR"
                        tags = listOf("Web")
                        startsAt = "2025-10-16T09:00"
                        endsAt = "2025-10-16T09:50"
                        complexity = "Beginner"
                        feedbackId = "feedback-1"
                        type = "conference"
                        links = emptyList()
                        speakers = emptyList()
                        rooms = listOf(
                            buildGraphQLRoom {
                                id = "Titan"
                                name = "Titan"
                            }
                        )
                        override()
                    }
                )
            }
        }
        return data.sessions.nodes.single().sessionDetails.toSession()
    }
}
