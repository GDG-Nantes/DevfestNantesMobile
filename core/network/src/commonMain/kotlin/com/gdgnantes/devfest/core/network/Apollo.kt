package com.gdgnantes.devfest.core.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.cache.normalized.api.CacheKey
import com.apollographql.cache.normalized.api.CacheKeyGenerator
import com.apollographql.cache.normalized.api.CacheKeyGeneratorContext
import com.apollographql.cache.normalized.api.CacheResolver
import com.apollographql.cache.normalized.api.DefaultCacheResolver
import com.apollographql.cache.normalized.api.ResolverContext
import com.apollographql.cache.normalized.normalizedCache

val cacheKeyGenerator =
    object : CacheKeyGenerator {
        override fun cacheKeyForObject(
            obj: Map<String, Any?>,
            context: CacheKeyGeneratorContext
        ): CacheKey? {
            if (obj.containsKey("id")) {
                return CacheKey(obj.get("id").toString())
            }

            return null
        }
    }

val cacheResolver =
    object : CacheResolver {
        override fun resolveField(context: ResolverContext): Any? {
            val id = context.field.argumentValue("id", context.variables).getOrNull()?.toString()
            if (id != null) {
                return CacheKey(id)
            }

            return DefaultCacheResolver.resolveField(context)
        }
    }
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
