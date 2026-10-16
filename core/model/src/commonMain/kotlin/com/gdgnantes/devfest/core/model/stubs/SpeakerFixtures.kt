package com.gdgnantes.devfest.core.model.stubs

import com.gdgnantes.devfest.core.model.SocialItem
import com.gdgnantes.devfest.core.model.SocialType
import com.gdgnantes.devfest.core.model.Speaker

/**
 * Fixed, hand-written speakers shared by previews, the mocked store and tests.
 *
 * Ids are stable. [farid] is the "everything optional is missing" speaker; [alice] carries one
 * social item of every [SocialType].
 */
object SpeakerFixtures {
    private fun social(type: SocialType, link: String): SocialItem =
        SocialItem.Builder().setType(type).setLink(link).build()

    val alice =
        Speaker(
            id = "speaker-alice",
            bio = LOREM_IPSUM,
            company = "Loire Labs",
            companyLogoUrl = null,
            city = "Nantes, France",
            name = "Alice Moreau",
            photoUrl = RANDOM_IMAGE_URL,
            socials =
            listOf(
                social(SocialType.GITHUB, "alice-moreau"),
                social(SocialType.LINKEDIN, "alice-moreau"),
                social(SocialType.TWITTER, "@alicemoreau"),
                social(SocialType.FACEBOOK, "alice.moreau"),
                social(SocialType.WEBSITE, "https://alice.example.org")
            )
        )

    val bruno =
        Speaker(
            id = "speaker-bruno",
            bio = LOREM_IPSUM,
            company = "Atlantique Software",
            companyLogoUrl = null,
            city = "Rennes, France",
            name = "Bruno Lefèvre",
            photoUrl = RANDOM_IMAGE_URL,
            socials = listOf(social(SocialType.TWITTER, "@brunolefevre"))
        )

    val chloe =
        Speaker(
            id = "speaker-chloe",
            bio = LOREM_IPSUM,
            company = "Mobile Factory",
            companyLogoUrl = null,
            city = "Paris, France",
            name = "Chloé Bernard",
            photoUrl = RANDOM_IMAGE_URL,
            socials = listOf(social(SocialType.LINKEDIN, "chloe-bernard"))
        )

    val david =
        Speaker(
            id = "speaker-david",
            bio = LOREM_IPSUM,
            company = "Cloud Harbor",
            companyLogoUrl = null,
            city = "Lyon, France",
            name = "David Nguyen",
            photoUrl = RANDOM_IMAGE_URL,
            socials = listOf(social(SocialType.GITHUB, "dnguyen"))
        )

    val emma =
        Speaker(
            id = "speaker-emma",
            bio = LOREM_IPSUM,
            company = "Git Garden",
            companyLogoUrl = null,
            city = "Milano, Italy",
            name = "Emma Rossi",
            photoUrl = RANDOM_IMAGE_URL,
            socials = listOf(social(SocialType.WEBSITE, "https://emma.example.org"))
        )

    val farid =
        Speaker(
            id = "speaker-farid",
            bio = null,
            company = null,
            companyLogoUrl = null,
            city = null,
            name = "Farid Haddad",
            photoUrl = null,
            socials = null
        )

    val all: List<Speaker> = listOf(alice, bruno, chloe, david, emma, farid)
}
