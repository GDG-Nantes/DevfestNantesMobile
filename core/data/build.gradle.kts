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
            implementation(libs.koin.core)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.preference)
            implementation(libs.androidx.core.ktx)
        }
    }
}

// D-15: the live-network canary (GraphQLStoreLiveNetworkTest) only runs on demand with
// -PliveNetworkTests, so the default jvmTest run is offline and deterministic.
tasks.named<Test>("jvmTest") {
    if (!providers.gradleProperty("liveNetworkTests").isPresent) {
        filter {
            excludeTestsMatching("*LiveNetwork*")
        }
    }
}
