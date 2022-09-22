package com.gdgnantes.devfest.model

data class SocialItem private constructor(
    val type: SocialType? = null,
    val link: String? = null
) {

    class Builder {
        private var _link: String? = null
        private var _type: SocialType? = null

        fun setLink(link: String): Builder = apply { _link = link }
        fun setType(type: SocialType): Builder = apply { _type = type }
        fun build(): SocialItem {
            return _link?.run { formatLink(this, _type) }
                .run { SocialItem(_type, this) }
        }

        private fun formatLink(link: String, type: SocialType?): String {
            return if (type != null) {
                if (type == SocialType.TWITTER) {
                    formatTwitterHandle(link)
                } else {
                    link
                }.run {
                    if (startsWith(type.baseUrl)) {
                        this
                    } else {
                        "${type.baseUrl}/$this"
                    }
                }
            } else {
                link
            }
        }

        private fun formatTwitterHandle(link: String): String {
            return if (link.startsWith("@")) {
                link.substring(1)
            } else {
                link
            }
        }
    }
}

enum class SocialType {
    GITHUB,
    LINKEDIN,
    TWITTER,
    FACEBOOK,
    WEBSITE;

    val baseUrl: String
        get() = when (this) {
            GITHUB -> "https://github.com/"
            LINKEDIN -> "https://www.linkedin.com/"
            TWITTER -> "https://twitter.com/"
            FACEBOOK -> "https://www.facebook.com/"
            WEBSITE -> ""
        }
}