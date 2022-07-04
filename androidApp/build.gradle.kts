plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

val composeCompilerVersion = "1.2.0"

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.gdgnantes.devfest.android"
        minSdk = 23
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val daggerVersion: String by rootProject.extra
val kotlinVersion: String by rootProject.extra
val composeVersion = "1.2.0-rc03"

dependencies {
    implementation(project(":shared"))
    implementation(project(":store"))
    implementation(project(":storeMocked"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3-native-mt")

    // Compose
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.4.0")
    // Compose Material Design
    implementation("androidx.compose.material:material:$composeVersion")
    // Animations
    implementation("androidx.compose.animation:animation:$composeVersion")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    // Integration with ViewModels
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha13")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    // Debug
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:$daggerVersion")
    kapt("com.google.dagger:hilt-compiler:$daggerVersion")
    // For instrumentation tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kaptAndroidTest("com.google.dagger:hilt-compiler:$daggerVersion")
    // For local unit tests
    testImplementation("com.google.dagger:hilt-android-testing:$daggerVersion")
    kaptTest("com.google.dagger:hilt-compiler:$daggerVersion")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
}
