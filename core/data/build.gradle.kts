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
            implementation(libs.kotlinx.serialization.json)
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
// D-09: the agenda snapshot recorder (AgendaSnapshotRecorder) is gated the same way, with
// -PrecordAgendaSnapshot; scripts/record-agenda-snapshot.sh is the documented entry point.
tasks.named<Test>("jvmTest") {
    val recordAgendaSnapshot = providers.gradleProperty("recordAgendaSnapshot").isPresent
    filter {
        if (!providers.gradleProperty("liveNetworkTests").isPresent) {
            excludeTestsMatching("*LiveNetwork*")
        }
        if (!recordAgendaSnapshot) {
            excludeTestsMatching("*AgendaSnapshotRecorder*")
        }
    }
    systemProperty(
        "devfest.dataTestdata",
        layout.projectDirectory.dir("testdata").asFile.absolutePath,
    )
    systemProperty(
        "devfest.domainTestdata",
        rootProject.layout.projectDirectory.dir("core/domain/testdata").asFile.absolutePath,
    )
    systemProperty("devfest.recordAgendaSnapshot", recordAgendaSnapshot.toString())
    // Recorded data is test input. fileTree tolerates a missing directory (plan 07-04 creates the
    // neutral snapshots later); only the three snapshot files are tracked, not the whole directory,
    // so goldens written there by other modules do not invalidate this task.
    inputs.files(fileTree(layout.projectDirectory.dir("testdata")))
        .withPropertyName("recordedGraphqlTestdata")
        .optional()
    inputs.files(
        fileTree(rootProject.layout.projectDirectory.dir("core/domain/testdata/agenda")) {
            include("*.snapshot.json")
        },
    ).withPropertyName("neutralAgendaSnapshots").optional()
    if (recordAgendaSnapshot) {
        // A recording run must always execute, even when the recorded files are unchanged.
        outputs.upToDateWhen { false }
    }
}
