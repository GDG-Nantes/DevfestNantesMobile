package com.gdgnantes.devfest.core.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.cache.normalized.normalizedCache

val apolloClient =
    ApolloClient.Builder()
        .serverUrl("https://confetti-app.dev/graphql")
        .httpHeaders(listOf(HttpHeader("conference", "devfestnantes2025")))
        .normalizedCache(
            normalizedCache,
            cacheKeyGenerator = cacheKeyGenerator,
            cacheResolver = cacheResolver
        )
        .build()
