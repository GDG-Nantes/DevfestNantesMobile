package com.gdgnantes.devfest.core.data.di

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.graphql.GraphQLStore
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Koin bindings of `:core:data`. The store is one shared instance on every platform; the concrete
 * [GraphQLStore] is internal to this module, which is why the definition lives here. Declared
 * concrete-primary with `bind` so Koin `verify()` inspects the implementation constructor.
 * Platform-specific bindings (preferences, bookmarks) come from [dataPlatformModule].
 */
@HiddenFromObjC
val dataModule: Module =
    module {
        includes(dataPlatformModule)
        single { GraphQLStore(get()) } bind DevFestNantesStore::class
    }

/** Platform-specific data bindings, one `actual` per target. */
internal expect val dataPlatformModule: Module
