package com.gdgnantes.devfest.model.stubs

import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.SocialType

val SOCIAL_ITEMS_STUBS: List<SocialItem> =
    listOf(
        SocialItem.Builder().setType(SocialType.TWITTER)
            .setLink("https://twitter.com/devfestnantes")
            .build(),
        SocialItem.Builder().setType(SocialType.WEBSITE).setLink("https://devfest.gdgnantes.com/")
            .build()
    )
