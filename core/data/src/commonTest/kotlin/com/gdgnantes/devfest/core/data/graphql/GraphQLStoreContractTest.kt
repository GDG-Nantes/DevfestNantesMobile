package com.gdgnantes.devfest.core.data.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.mockserver.MockServer
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.DevFestNantesStoreContractTest
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.PartnerCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The shared store contract over [GraphQLStore] talking to an Apollo mock server that serves the
 * recorded responses. No normalized cache here: cache behavior is pinned in
 * [GraphQLStoreMockServerTest]. Value assertions are literals from the recordings.
 */
class GraphQLStoreContractTest : DevFestNantesStoreContractTest() {

    private var server: MockServer? = null
    private var client: ApolloClient? = null

    override suspend fun createStore(): DevFestNantesStore {
        val mockServer = MockServer.Builder().handler(RecordedResponsesHandler()).build()
        val apolloClient = ApolloClient.Builder().serverUrl(mockServer.url()).build()
        server = mockServer
        client = apolloClient
        return GraphQLStore(apolloClient)
    }

    override fun releaseStore() {
        client?.close()
        server?.close()
        client = null
        server = null
    }

    override val knownSessionId: String = RecordedResponsesHandler.RECORDED_SESSION_ID

    override val knownSpeakerId: String = "eduardo_san_martin_morote"

    override val knownRoomId: String = "Titan"

    @Test
    fun the_recorded_sessions_start_with_the_opening_keynote() = runTest {
        val sessions = createStore().sessions.first()

        assertEquals(5, sessions.size)
        assertEquals("Opening Keynote - Advice Well Taken", sessions.first().title)
    }

    @Test
    fun a_recorded_speaker_has_its_recorded_name() = runTest {
        assertEquals(
            "Eduardo San Martin Morote",
            createStore().getSpeaker("eduardo_san_martin_morote")?.name
        )
    }

    @Test
    fun the_recorded_rooms_are_eight() = runTest {
        assertEquals(8, createStore().rooms.first().size)
    }

    @Test
    fun the_recorded_partner_categories_are_the_five_groups() = runTest {
        assertEquals(
            setOf(
                PartnerCategory.PXL,
                PartnerCategory.PLATINIUM,
                PartnerCategory.GOLD,
                PartnerCategory.VIRTUAL,
                PartnerCategory.VELOTYPE
            ),
            createStore().partners.first().keys
        )
    }

    @Test
    fun the_recorded_venue_descriptions_are_english_and_french() = runTest {
        val store = createStore()

        assertTrue(
            store.getVenue(ContentLanguage.ENGLISH).description
                .startsWith("Located in the center of Nantes")
        )
        assertTrue(
            store.getVenue(ContentLanguage.FRENCH).description
                .startsWith("Située en plein cœur de ville")
        )
    }
}
