plugins {
    alias(libs.plugins.appollo) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kmp.native.coroutines) apply false
    alias(libs.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.secrets) apply false
}

// Kover is report-only (D-18): the root merges the 13 production modules into one report.
// There is deliberately no verify rule and no threshold. :core:testing is test-support code
// and is not aggregated.
dependencies {
    kover(project(":androidApp"))
    kover(project(":shared"))
    kover(project(":core:model"))
    kover(project(":core:network"))
    kover(project(":core:analytics"))
    kover(project(":core:data"))
    kover(project(":core:ui"))
    kover(project(":feature:venue"))
    kover(project(":feature:about"))
    kover(project(":feature:settings"))
    kover(project(":feature:speakers"))
    kover(project(":feature:agenda"))
    kover(project(":feature:session-detail"))
}

kover {
    reports {
        filters {
            excludes {
                androidGeneratedClasses()
                packages("com.gdgnantes.devfest.core.network.graphql")
                classes("*ComposableSingletons*")
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
            }
        }
    }
}
