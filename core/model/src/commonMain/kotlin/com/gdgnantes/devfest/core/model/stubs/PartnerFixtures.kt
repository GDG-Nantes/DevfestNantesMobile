package com.gdgnantes.devfest.core.model.stubs

import com.gdgnantes.devfest.core.model.Partner
import com.gdgnantes.devfest.core.model.PartnerCategory

/** Fixed, hand-written partners covering every [PartnerCategory]. All URLs are on example.org. */
object PartnerFixtures {
    private fun partner(name: String, url: String): Partner =
        Partner(name = name, logoUrl = RANDOM_IMAGE_URL, url = url)

    val pixelStudio = partner("Pixel Studio", "https://pixel.example.org")
    val loireCloud = partner("Loire Cloud", "https://loire-cloud.example.org")
    val atlanticMobile = partner("Atlantic Mobile", "https://atlantic-mobile.example.org")
    val nantesData = partner("Nantes Data", "https://nantes-data.example.org")
    val remoteFirst = partner("Remote First", "https://remote-first.example.org")
    val communityHub = partner("Community Hub", "https://community-hub.example.org")
    val veloPrint = partner("Velo Print", "https://velo-print.example.org")

    val byCategory: Map<PartnerCategory, List<Partner>> =
        mapOf(
            PartnerCategory.PXL to listOf(pixelStudio),
            PartnerCategory.PLATINIUM to listOf(loireCloud, atlanticMobile),
            PartnerCategory.GOLD to listOf(nantesData),
            PartnerCategory.VIRTUAL to listOf(remoteFirst),
            PartnerCategory.PARTNERS to listOf(communityHub),
            PartnerCategory.VELOTYPE to listOf(veloPrint)
        )
}
