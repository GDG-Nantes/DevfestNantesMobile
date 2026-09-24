package com.gdgnantes.devfest.store.graphql

import com.apollographql.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory

val normalizedCache: NormalizedCacheFactory =
    MemoryCacheFactory(10 * 1024 * 1024)
        .chain(
            SqlNormalizedCacheFactory("apollo.db")
        )
