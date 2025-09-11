import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.secrets)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom("$rootDir/linters/detekt-config.yml")
}
tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}

android {
    compileSdk = AndroidSdk.compile
    defaultConfig {
        applicationId = "com.gdgnantes.devfest.mobile.androidapp"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
        versionCode = 36
        versionName = "2025.09.02"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "OPEN_FEEDBACK_ENABLED", "\"false\"")
        buildConfigField("String", "OPEN_FEEDBACK_PROJECT_ID", "\"SECRET\"")
        buildConfigField("String", "OPEN_FEEDBACK_FIREBASE_PROJECT_ID", "\"SECRET\"")
        buildConfigField("String", "OPEN_FEEDBACK_FIREBASE_APPLICATION_ID", "\"SECRET\"")
        buildConfigField("String", "OPEN_FEEDBACK_FIREBASE_API_KEY", "\"SECRET\"")
        buildConfigField("String", "OPEN_FEEDBACK_FIREBASE_DATABASE_URL", "\"SECRET\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEBUG"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()

        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.Experimental",
        )
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
    namespace = "com.gdgnantes.devfest.androidapp"
}

dependencies {
    detektPlugins(libs.detekt.fomatting)

    implementation(project(":shared"))

    implementation(libs.bundles.accompanist)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.activity.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.preference)
    implementation(libs.androidx.splashscreen)

    implementation(libs.coil.compose)

    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)
    androidTestImplementation(libs.dagger.hilt.android.testing)
    kspAndroidTest(libs.dagger.hilt.compiler)
    testImplementation(libs.dagger.hilt.android.testing)
    kspTest(libs.dagger.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.perf.ktx)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.material)
    implementation(libs.openfeedback.m3)
    implementation(libs.openfeedback.viewmodel)
    implementation(libs.timber)

    debugImplementation(libs.bundles.debug)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.bundles.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.test.espresso.contrib) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.test.espresso.intents) {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.truth)
}
