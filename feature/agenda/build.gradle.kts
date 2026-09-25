plugins {
    id("devfest.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.bundles.accompanist)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.timber)
}
