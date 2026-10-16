package com.gdgnantes.devfest.core.data.graphql

import com.apollographql.mockserver.MockRequest
import com.apollographql.mockserver.MockRequestBase
import com.apollographql.mockserver.MockResponse
import com.apollographql.mockserver.MockServerHandler

/**
 * Serves the recorded responses by GraphQL operation name instead of by request order, so a test
 * can call the store in any order and any number of times. An operation without a recording gets
 * a 500, never a silent success.
 *
 * Apollo posts `{"operationName":"GetSessions","variables":{...},"query":"..."}`.
 */
class RecordedResponsesHandler : MockServerHandler {

    override fun handle(request: MockRequestBase): MockResponse {
        val body = (request as? MockRequest)?.body?.utf8().orEmpty()
        val operation = OPERATION_NAME.find(body)?.groupValues?.get(1)

        val json = when (operation) {
            "GetRooms" -> RecordedResponses.ROOMS
            "GetSessions" -> RecordedResponses.SESSIONS
            "GetSpeakers" -> RecordedResponses.SPEAKERS
            "GetPartnerGroups" -> RecordedResponses.PARTNER_GROUPS
            "GetVenue" ->
                if (body.contains("\"language\":\"fr\"")) {
                    RecordedResponses.VENUE_FR
                } else {
                    RecordedResponses.VENUE_EN
                }
            "GetSession" ->
                if (body.contains("\"id\":\"$RECORDED_SESSION_ID\"")) {
                    RecordedResponses.SESSION
                } else {
                    NOT_FOUND
                }
            else -> return MockResponse.Builder().statusCode(500).build()
        }

        return MockResponse.Builder()
            .statusCode(200)
            .addHeader("Content-Type", "application/json")
            .body(json)
            .build()
    }

    companion object {
        /** The session id of [RecordedResponses.SESSION]; it is also one of [RecordedResponses.SESSIONS]. */
        const val RECORDED_SESSION_ID = "thefutureofroutingwiththenavigationapi"

        private val OPERATION_NAME = Regex("\"operationName\"\\s*:\\s*\"(\\w+)\"")

        private const val NOT_FOUND = """{"data":null,"errors":[{"message":"not found"}]}"""
    }
}
