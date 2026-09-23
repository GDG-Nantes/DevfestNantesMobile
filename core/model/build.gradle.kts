plugins {
    id("devfest.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
