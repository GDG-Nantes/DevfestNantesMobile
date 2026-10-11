plugins {
    id("devfest.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics.ktx)
            implementation(libs.firebase.perf.ktx)
            implementation(libs.firebase.crashlytics.ktx)
            implementation(libs.timber)
        }
    }
}
