package com.gdgnantes.devfest.store.graphql

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.CompiledField
import com.apollographql.apollo3.api.Executable
import com.apollographql.apollo3.api.http.HttpHeader
import com.apollographql.apollo3.cache.normalized.api.CacheKey
import com.apollographql.apollo3.cache.normalized.api.CacheKeyGenerator
import com.apollographql.apollo3.cache.normalized.api.CacheKeyGeneratorContext
import com.apollographql.apollo3.cache.normalized.api.CacheResolver
import com.apollographql.apollo3.cache.normalized.api.DefaultCacheResolver
import com.apollographql.apollo3.cache.normalized.normalizedCache

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
        override fun resolveField(
            field: CompiledField,
            variables: Executable.Variables,
            parent: Map<String, Any?>,
            parentId: String
        ): Any? {
            val id = field.resolveArgument("id", variables)?.toString()
            if (id != null) {
                return CacheKey(id)
            }

            return DefaultCacheResolver.resolveField(field, variables, parent, parentId)
        }
    }
val apolloClient =
    ApolloClient.Builder()
        .serverUrl("https://confetti-app.dev/graphql")
        .httpHeaders(listOf(HttpHeader("conference", "devfestnantes2023")))
        .normalizedCache(
            normalizedCache,
            cacheKeyGenerator = cacheKeyGenerator,
            cacheResolver = cacheResolver
        )
        .build()
