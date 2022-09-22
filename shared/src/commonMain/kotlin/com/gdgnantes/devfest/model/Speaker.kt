package com.gdgnantes.devfest.model

data class Speaker(
    val id: String = "",
    val bio: String? = null,
    val company: String? = null,
    val companyLogoUrl: String? = null,
    val city: String? = null,
    val name: String = "",
    val photoUrl: String? = null,
    val socials: List<SocialItem>? = null,
) {

    fun getFullNameAndCompany(): String {
        return name + if (company.isNullOrBlank()) "" else ", " + this.company
    }

}
