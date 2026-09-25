plugins {
    id("devfest.android.application")
    id("devfest.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.secrets)
}

android {
    defaultConfig {
        applicationId = "com.gdgnantes.devfest.mobile.androidapp"
        versionCode = 37
        versionName = "2025.10.00"

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

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/AL2.0")
            excludes.add("/META-INF/LGPL2.1")
        }
    }

    namespace = "com.gdgnantes.devfest.androidapp"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:analytics"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:venue"))
    implementation(project(":feature:about"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:speakers"))

    implementation(libs.bundles.accompanist)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.activity.ktx)

    implementation(libs.androidx.compose.activity)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.navigation)

    implementation(libs.androidx.preference)
    implementation(libs.androidx.splashscreen)

    implementation(libs.coil.compose)

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
