plugins {
    id("devfest.android.library")
    id("devfest.android.hilt")
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:analytics"))

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.coil.compose)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}
