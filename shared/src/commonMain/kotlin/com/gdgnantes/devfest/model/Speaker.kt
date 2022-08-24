package com.gdgnantes.devfest.model

data class Speaker(
    val id: String = "",
    val bio: String? = null,
    val company: String? = null,
    val companyLogo: String? = null,
    val country: String? = null,
    val firstname: String = "",
    val photoUrl: String? = null,
    val socials: List<SocialsItem?>? = null,
    val surname: String?
) {

    fun getFullName(): String {
        return this.firstname + this.surname
    }

    fun getFullNameAndCompany(): String {
        return getFullName() + if (company.isNullOrBlank()) "" else ", " + this.company
    }

}
