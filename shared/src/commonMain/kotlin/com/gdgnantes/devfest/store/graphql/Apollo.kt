package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.normalizedCache

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql")
    .normalizedCache(normalizedCache)
    .build()