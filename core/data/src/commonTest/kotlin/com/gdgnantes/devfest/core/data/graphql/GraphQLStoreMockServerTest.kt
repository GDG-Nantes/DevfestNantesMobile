package com.gdgnantes.devfest.core.data.graphql

import com.apollographql.apollo.ApolloClient
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `GraphQLStore` over a real [ApolloClient] talking to an Apollo `MockServer` that serves the
 * recorded responses (D-13, D-14). Every test builds its own server and client and closes the
 * server, so nothing leaks between tests, runs or runtimes (each server binds its own port).
 */
class GraphQLStoreMockServerTest {

    private suspend fun withStore(
        body: suspend (store: GraphQLStore, server: MockServer) -> Unit
    ) {
        val server = MockServer()
        try {
            val client = ApolloClient.Builder().serverUrl(server.url()).build()
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

    private companion object {
        const val JSON = "application/json"
    }
}
