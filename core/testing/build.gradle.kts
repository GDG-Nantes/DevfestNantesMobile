plugins {
    id("devfest.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data"))
            api(project(":core:analytics"))
            api(libs.koin.core)
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            api(libs.junit)
        }
    }
}
