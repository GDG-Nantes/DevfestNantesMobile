package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory

val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
    .chain(SqlNormalizedCacheFactory("apollo.db"))

val apolloClient = ApolloClient.Builder()
    .serverUrl("https://graphql-dot-confetti-349319.uw.r.appspot.com/graphql")
    .normalizedCache(memoryFirstThenSqlCacheFactory)
    .build()