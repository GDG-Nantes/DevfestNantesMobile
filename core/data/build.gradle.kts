plugins {
    id("devfest.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}

kotlin {
    // Android host tests (testAndroidHostTest) are opted in for this module only: they also run
    // commonTest on the Android host, and 05-05 adds a Robolectric test here (RESEARCH Pattern 1).
    targets.withType(com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget::class.java)
        .configureEach {
            withHostTest {
                isIncludeAndroidResources = true
            }
        }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:network"))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.apollo.mockserver)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.preference)
            implementation(libs.androidx.core.ktx)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.robolectric)
            implementation(libs.junit)
            implementation(libs.androidx.test.core)
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
