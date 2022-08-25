package com.gdgnantes.devfest.model

data class SocialsItem(
    val type: SocialsType? = null,
    val link: String? = null
)

enum class SocialsType {
    GITHUB,
    LINKEDIN,
    TWITTER,
    FACEBOOK,
    WEBSITE
}