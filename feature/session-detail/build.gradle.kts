plugins {
    id("devfest.android.feature")
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config.ktx)
    implementation(libs.openfeedback.m3)
    implementation(libs.openfeedback.viewmodel)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.timber)
}
