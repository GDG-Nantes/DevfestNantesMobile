plugins {
    id("devfest.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data"))
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
    }
}
