plugins {
    id("devfest.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics.ktx)
            implementation(libs.firebase.perf.ktx)
            implementation(libs.timber)
            implementation(libs.dagger.hilt.android)
        }
    }
}
