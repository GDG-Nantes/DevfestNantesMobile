plugins {
    id("devfest.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.bundles.accompanist)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.timber)
}

// Characterization goldens live next to the shared domain tests. A default run only reads them;
// -PrecordGoldens lets GoldenFiles write an absent golden (see core/domain/testdata/agenda/README.md).
val domainTestdata = rootProject.layout.projectDirectory.dir("core/domain/testdata")

tasks.withType<Test>().configureEach {
    systemProperty("devfest.domainTestdata", domainTestdata.asFile.absolutePath)
    inputs.dir(domainTestdata)
        .withPropertyName("domainTestdata")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    if (providers.gradleProperty("recordGoldens").isPresent) {
        systemProperty("devfest.recordGoldens", "true")
    }
}
