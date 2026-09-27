plugins {
    id("devfest.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:network"))
        }
        androidMain.dependencies {
            implementation(libs.dagger.hilt.android)
            implementation(libs.androidx.core.ktx)
        }
    }
}
