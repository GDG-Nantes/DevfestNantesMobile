plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.secrets) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}