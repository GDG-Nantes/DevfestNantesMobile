plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = AndroidSdk.compile
    defaultConfig {
        applicationId = "com.gdgnantes.devfest.android"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":shared"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3-native-mt")

    with(Compose) {
        implementation(activity)
        implementation(material3)
        implementation(materialIconsCore)
        implementation(materialIconsExtended)
        implementation(navigation)
        implementation(uiTooling)
        implementation(hiltNavigation)
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

    with(Tests) {
        testImplementation(junit)
        testImplementation(coroutinesTest)
        testImplementation(kotlinJUnit)
    }

    with(AndroidTests) {
        androidTestImplementation(androidXComposeUiTest)
        androidTestImplementation(androidXTestCoreKtx)
        androidTestImplementation(androidXTestRunner)
        androidTestImplementation(androidXTestRules)
        androidTestImplementation(androidXTestJUnitKtx)
        androidTestImplementation(androidXTestTruth)
        androidTestImplementation(androidXTestEspressoCore)
        androidTestImplementation(androidXTestEspressoContrib)
        androidTestImplementation(androidXTestEspressoIntents)
    }
}
