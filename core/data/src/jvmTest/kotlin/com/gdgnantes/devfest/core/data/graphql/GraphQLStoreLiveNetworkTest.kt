package com.gdgnantes.devfest.core.data.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpHeader
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.utils.toInstantOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Manual-only schema-drift canary: it talks to the live confetti-app.dev backend, so it is
 * excluded from the default `:core:data:jvmTest` run (the CI unit run stays offline and
 * deterministic). Run it explicitly with:
 *
 *     ./gradlew :core:data:jvmTest -PliveNetworkTests --tests '*LiveNetwork*'
 */
class GraphQLStoreLiveNetworkTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var store: GraphQLStore

    @BeforeTest
    fun setUp() {
        apolloClient = ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
            .httpHeaders(listOf(HttpHeader("conference", "devfestnantes2025")))
            .build()
        store = GraphQLStore(apolloClient)
    }

    @Test
    fun agenda_flow_emits_agenda() = runTest {
        val agenda = store.agenda.first()
        assertNotNull(agenda)
    }

    @Test
    fun partners_flow_emits_partners() = runTest {
        val partners = store.partners.first()
        assertNotNull(partners)
    }

    @Test
    fun getRoom_returns_room_or_null() = runTest {
        val room = store.getRoom("someId")
        // Accept null or Room, just check no crash
    }

    @Test
    fun rooms_flow_emits_rooms() = runTest {
        val rooms = store.rooms.first()
        assertNotNull(rooms)
    }

    @Test
    fun getSession_returns_session_or_null() = runTest {
        val session = store.getSession("someId")
        // Accept null or Session, just check no crash
    }

    @Test
    fun sessions_flow_emits_sessions() = runTest {
        val sessions = store.sessions.first()
        assertNotNull(sessions)
    }

    @Test
    fun getSpeaker_returns_speaker_or_null() = runTest {
        val speaker = store.getSpeaker("someId")
        // Accept null or Speaker, just check no crash
    }

    @Test
    fun getSpeakerSessions_returns_sessions() = runTest {
        val sessions = store.getSpeakerSessions("someId")
        assertNotNull(sessions)
    }

    @Test
    fun speakers_flow_emits_speakers() = runTest {
        val speakers = store.speakers.first()
        assertNotNull(speakers)
    }

    @Test
    fun speakers_flow_emits_non_empty_speakers_list() = runTest {
        val speakers = store.speakers.first()
        assertNotNull(speakers)
        assert(speakers.isNotEmpty()) { "Speakers list should not be empty" }
    }

    @Test
    fun getVenue_returns_venue() = runTest {
        val venue = store.getVenue(ContentLanguage.ENGLISH)
        assertNotNull(venue)
    }

    @Test
    fun scheduleSlot_dates_are_parseable_by_the_shared_parser() = runTest {
        val sessions = store.sessions.first()
        for (session in sessions) {
            val slot = session.scheduleSlot
            assertNotNull(slot.startDate.toInstantOrNull(), "Unparseable start date: ${slot.startDate}")
            assertNotNull(slot.endDate.toInstantOrNull(), "Unparseable end date: ${slot.endDate}")
        }
    }
}
