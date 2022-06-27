plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

val composeCompilerVersion = "1.1.1"

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
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeCompilerVersion
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
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val daggerVersion: String by rootProject.extra

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
    implementation("androidx.compose.material:material:$composeCompilerVersion")
    // Animations
    implementation("androidx.compose.animation:animation:$composeCompilerVersion")
    // Tooling support (Previews, etc.)
    implementation("androidx.compose.ui:ui-tooling:$composeCompilerVersion")
    // Integration with ViewModels
    implementation("androidx.compose.ui:ui:$composeCompilerVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha13")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeCompilerVersion")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeCompilerVersion")
    // Debug
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeCompilerVersion")

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
