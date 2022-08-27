package com.gdgnantes.devfest.model.stubs

import com.gdgnantes.devfest.model.SocialsItem
import com.gdgnantes.devfest.model.SocialsType
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val socialItemStubs: List<SocialsItem> = listOf(
    SocialsItem(SocialsType.TWITTER, "https://twitter.com/devfestnantes"),
    SocialsItem(SocialsType.WEBSITE, "https://devfest.gdgnantes.com/")
)