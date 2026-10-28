plugins {
    id("devfest.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            // api: later use-case constructors expose DevFestNantesStore, BookmarksStore and SessionFiltersStore.
            api(project(":core:data"))
            implementation(libs.koin.core)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
