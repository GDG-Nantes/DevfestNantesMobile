package com.gdgnantes.devfest.core.data.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.normalizedCache
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueError
import com.apollographql.mockserver.enqueueString
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.stubs.buildVenueStub
import com.gdgnantes.devfest.core.network.cacheKeyGenerator
import com.gdgnantes.devfest.core.network.cacheResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `GraphQLStore` over a real [ApolloClient] talking to an Apollo `MockServer` that serves the
 * recorded responses (D-13, D-14). Every test builds its own server and client and closes the
 * server, so nothing leaks between tests, runs or runtimes (each server binds its own port).
 */
class GraphQLStoreMockServerTest {

    /**
     * [cached] builds the client like production does (normalized cache with the production cache
     * keys) but over an in-memory cache, so no test touches the SQLite file of the app.
     */
    private suspend fun withStore(
        cached: Boolean = false,
        body: suspend (store: GraphQLStore, server: MockServer) -> Unit
    ) {
        val server = MockServer()
        try {
            val builder = ApolloClient.Builder().serverUrl(server.url())
            if (cached) {
                builder.normalizedCache(MemoryCacheFactory(), cacheKeyGenerator, cacheResolver)
            }
            val client = builder.build()
            try {
                body(GraphQLStore(client), server)
            } finally {
                client.close()
            }
        } finally {
            server.close()
        }
    }

    private fun MockServer.serve(json: String) = enqueueString(json, contentType = JSON)

    @Test
    fun rooms_flow_maps_and_sorts_recorded_rooms() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.ROOMS)

            val rooms = store.rooms.first()

            // Ordered by RoomSortIndex; ids outside the known list ("Tour Bretagne", "Unconf")
            // share Int.MAX_VALUE and keep their recorded order at the end.
            assertEquals(
                listOf(
                    "Jules Verne",
                    "Titan",
                    "Belem",
                    "Les Machines",
                    "Hangar",
                    "L'Atelier",
                    "Tour Bretagne",
                    "Unconf"
                ),
                rooms.map { it.id }
            )
            assertEquals(
                listOf(0, 1, 2, 4, 5, 6, Int.MAX_VALUE, Int.MAX_VALUE),
                rooms.map { it.sortIndex }
            )
            assertEquals("L'Atelier", rooms.first { it.id == "L'Atelier" }.name)
        }
    }

    @Test
    fun sessions_flow_maps_recorded_sessions() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.SESSIONS)

            val sessions = store.sessions.first()

            assertEquals(5, sessions.size)
            val keynote = sessions.first()
            assertEquals("keynotedouverture", keynote.id)
            assertEquals("Opening Keynote - Advice Well Taken", keynote.title)
            assertEquals("2025-10-16T09:00:00+02:00", keynote.scheduleSlot.startDate)
            assertEquals("2025-10-16T09:50:00+02:00", keynote.scheduleSlot.endDate)
            assertEquals("Jules Verne", keynote.room?.id)
        }
    }

    // region flows and getters on recorded data

    @Test
    fun speakers_flow_maps_recorded_speakers() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.SPEAKERS)

            val speakers = store.speakers.first()

            assertEquals(7, speakers.size)
            assertEquals("dasha_ilina", speakers.first().id)
            assertEquals("Dasha Ilina", speakers.first().name)
        }
    }

    @Test
    fun partners_flow_maps_recorded_partner_groups() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.PARTNER_GROUPS)

            val partners = store.partners.first()

            assertEquals(
                setOf(
                    PartnerCategory.PXL,
                    PartnerCategory.PLATINIUM,
                    PartnerCategory.GOLD,
                    PartnerCategory.VIRTUAL,
                    PartnerCategory.VELOTYPE
                ),
                partners.keys
            )
            assertEquals("Epitech", partners.getValue(PartnerCategory.GOLD).single().name)
        }
    }

    @Test
    fun agenda_flow_groups_the_recorded_sessions_by_day() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.SESSIONS)

            val agenda = store.agenda.first()

            assertEquals(listOf(1, 2), agenda.days.keys.toList())
            // The recording holds four sessions on day one and the 09:00 Friday codelab on day two.
            assertEquals(4, agenda.days.getValue(1).sessions.size)
            assertEquals(1, agenda.days.getValue(2).sessions.size)
        }
    }

    @Test
    fun getSession_getSpeaker_getRoom_and_getVenue_return_recorded_values() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.SESSION)
            server.serve(RecordedResponses.SPEAKERS)
            server.serve(RecordedResponses.ROOMS)
            server.serve(RecordedResponses.VENUE_FR)

            assertEquals(
                "The future of routing with the Navigation API",
                store.getSession("thefutureofroutingwiththenavigationapi")?.title
            )
            assertEquals("Eduardo San Martin Morote", store.getSpeaker("eduardo_san_martin_morote")?.name)
            assertEquals("Titan", store.getRoom("Titan")?.name)
            assertEquals("Cité des Congrès de Nantes", store.getVenue(ContentLanguage.FRENCH).name)
        }
    }

    @Test
    fun getSpeakerSessions_filters_by_speaker_id() = runTest {
        withStore { store, server ->
            repeat(3) { server.serve(RecordedResponses.SESSIONS) }

            assertEquals(
                listOf("laguerredespromptsattaquesdefensesauroyaumedesllm"),
                store.getSpeakerSessions("francois_nollen").map { it.id }
            )
            assertEquals(
                listOf("lalegendedeslecteursdecranuneodysseesensorielleaucurdenosapps"),
                store.getSpeakerSessions("oceane_gillard").map { it.id }
            )
            assertEquals(emptyList(), store.getSpeakerSessions("nobody"))
        }
    }

    // endregion

    // region cache behavior (D-14)

    @Test
    fun sessions_cache_miss_emits_only_the_network_result() = runTest {
        withStore(cached = true) { store, server ->
            server.serve(RecordedResponses.SESSIONS)

            val emissions = store.sessions.toList()

            assertEquals(1, emissions.size)
            assertEquals(5, emissions.single().size)
        }
    }

    @Test
    fun network_error_after_cached_success_emits_only_cached_sessions() = runTest {
        withStore(cached = true) { store, server ->
            server.serve(RecordedResponses.SESSIONS)
            val first = store.sessions.toList()
            assertEquals(1, first.size)

            server.enqueueError(statusCode = 500)
            val second = store.sessions.toList()

            // The Phase 2 map -> mapNotNull fix: the failed network leg is dropped, the cached
            // list survives, and an empty list never replaces it.
            assertEquals(first, second)
            assertTrue(second.none { it.isEmpty() })
        }
    }

    @Test
    fun network_error_after_cached_success_emits_only_cached_speakers() = runTest {
        withStore(cached = true) { store, server ->
            server.serve(RecordedResponses.SPEAKERS)
            val first = store.speakers.toList()
            assertEquals(1, first.size)

            server.enqueueError(statusCode = 500)
            val second = store.speakers.toList()

            assertEquals(first, second)
            assertTrue(second.none { it.isEmpty() })
        }
    }

    @Test
    fun network_error_after_cached_success_emits_only_cached_partners() = runTest {
        withStore(cached = true) { store, server ->
            server.serve(RecordedResponses.PARTNER_GROUPS)
            val first = store.partners.toList()
            assertEquals(1, first.size)

            server.enqueueError(statusCode = 500)
            val second = store.partners.toList()

            assertEquals(first, second)
            assertTrue(second.none { it.isEmpty() })
        }
    }

    @Test
    fun network_error_after_cached_success_emits_only_cached_rooms() = runTest {
        withStore(cached = true) { store, server ->
            server.serve(RecordedResponses.ROOMS)
            val first = store.rooms.toList()
            assertEquals(1, first.size)

            server.enqueueError(statusCode = 500)
            val second = store.rooms.toList()

            assertEquals(first, second)
            assertTrue(second.none { it.isEmpty() })
        }
    }

    @Test
    fun cached_then_network_success_emits_twice() = runTest {
        withStore(cached = true) { store, server ->
            server.serve(RecordedResponses.SESSIONS)
            val first = store.sessions.toList()
            assertEquals(1, first.size)

            server.serve(RecordedResponses.SESSIONS)
            val second = store.sessions.toList()

            // Cache first, then the network result.
            assertEquals(2, second.size)
            assertEquals(first.single(), second[0])
            assertEquals(first.single(), second[1])
        }
    }

    @Test
    fun network_error_without_any_cache_emits_nothing() = runTest {
        withStore(cached = true) { store, server ->
            server.enqueueError(statusCode = 500)

            assertEquals(emptyList(), store.sessions.toList())
        }
    }

    // endregion

    // region fallbacks

    @Test
    fun getVenue_falls_back_to_the_offline_stub_on_error() = runTest {
        withStore { store, server ->
            server.enqueueError(statusCode = 500)
            server.enqueueError(statusCode = 500)

            assertEquals(buildVenueStub(ContentLanguage.FRENCH), store.getVenue(ContentLanguage.FRENCH))
            assertEquals(buildVenueStub(ContentLanguage.ENGLISH), store.getVenue(ContentLanguage.ENGLISH))
        }
    }

    @Test
    fun the_offline_venue_stub_differs_from_the_recorded_venue() = runTest {
        withStore { store, server ->
            server.serve(RecordedResponses.VENUE_EN)

            assertNotEquals(buildVenueStub(ContentLanguage.ENGLISH), store.getVenue(ContentLanguage.ENGLISH))
        }
    }

    @Test
    fun getters_return_null_or_empty_on_error() = runTest {
        withStore { store, server ->
            repeat(4) { server.enqueueError(statusCode = 500) }

            assertNull(store.getSession("thefutureofroutingwiththenavigationapi"))
            assertNull(store.getSpeaker("eduardo_san_martin_morote"))
            assertNull(store.getRoom("Titan"))
            assertEquals(emptyList(), store.getSpeakerSessions("francois_nollen"))
        }
    }

    // endregion

    private companion object {
        const val JSON = "application/json"
    }
}
