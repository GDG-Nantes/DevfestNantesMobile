plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

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
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":shared"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3-native-mt")

    with(Compose) {
        implementation(activity)
        implementation(uiTooling)
        implementation(material3)
    }

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Versions.compose}")
    // Debug
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Versions.compose}")

    with(Dagger) {
        implementation(hilt)
        kapt(compiler)
        // For instrumentation tests
        androidTestImplementation(androidTesting)
        kaptAndroidTest(compiler)
        // For local unit tests
        testImplementation(androidTesting)
        kaptTest(compiler)
    }

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")
}
