package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory

val normalizedCache: NormalizedCacheFactory =
    MemoryCacheFactory(10 * 1024 * 1024)
        .chain(
            SqlNormalizedCacheFactory("apollo.db")
        )
