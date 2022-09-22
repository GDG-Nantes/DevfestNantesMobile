package com.gdgnantes.devfest.model.stubs

import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.SocialType
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val socialItemStubs: List<SocialItem> = listOf(
    SocialItem.Builder().setType(SocialType.TWITTER).setLink("https://twitter.com/devfestnantes")
        .build(),
    SocialItem.Builder().setType(SocialType.WEBSITE).setLink("https://devfest.gdgnantes.com/")
        .build()
)