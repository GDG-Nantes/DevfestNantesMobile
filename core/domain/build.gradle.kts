import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

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
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.serialization.json)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}

// The characterization data and goldens live next to the shared domain tests. A default run only
// reads them (see core/domain/testdata/agenda/README.md); this module does not forward
// -PrecordGoldens, so an absent golden fails the test and nothing is ever written here.
val domainTestData = layout.projectDirectory.dir("testdata")

tasks.named<Test>("jvmTest") {
    systemProperty("devfest.domainTestdata", domainTestData.asFile.absolutePath)
    inputs.dir(domainTestData)
        .withPropertyName("domainTestdata")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}

// A plain environment variable is not visible inside the iOS simulator process: only variables
// prefixed with SIMCTL_CHILD_ are forwarded by `simctl spawn`, with the prefix stripped.
tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_DOMAIN_TESTDATA", domainTestData.asFile.absolutePath)
    inputs.dir(domainTestData)
        .withPropertyName("domainTestdata")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
