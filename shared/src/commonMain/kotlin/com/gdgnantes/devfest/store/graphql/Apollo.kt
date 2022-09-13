package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql")
    .build()