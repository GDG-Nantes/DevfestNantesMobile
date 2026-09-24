import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.secrets)
}

configurations.configureEach {
    resolutionStrategy {
        // Kotlin 2.4.20's compiler emits @Metadata format 2.4.0, but Dagger/Hilt 2.57.2's
        // shaded room-compiler-processing bundles kotlin-metadata-jvm 2.2.20 (max readable
        // format 2.2.0), so hiltJavaCompileDebug fails with
        // "Provided Metadata instance has version 2.4.0, while maximum supported version is
        // 2.2.0." Force the reader library itself to the Kotlin version this stage bumps to;
        // kotlin-metadata-jvm's read API is designed to be forward-compatible across Kotlin
        // releases. Forced sibling of the Kotlin bump, same class as the ksp/kmpNativeCoroutines
        // bumps above — no Dagger/Hilt version change needed, and Hilt 2.59+ would force AGP 9
        // anyway (out of scope for this stage).
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")
    }
    resolutionStrategy.dependencySubstitution {
        // Firebase BOM 34.19.0 dropped `firebase-auth-ktx` from its <dependencyManagement>
        // constraints (KTX extensions were merged into the main artifacts years ago; Google
        // finally stopped publishing new -ktx releases after 23.2.1). This project never
        // depends on Firebase Auth directly — `io.openfeedback:openfeedback-viewmodel` pulls
        // it in transitively via `dev.gitlive:firebase-auth`, which still requests the
        // deprecated -ktx coordinate with no explicit version, so resolution fails once the
        // BOM stops supplying one. Redirect to the plain, BOM-managed `firebase-auth`
        // artifact (same API surface, KTX extensions included) rather than pinning the
        // frozen 23.2.1 -ktx artifact, which would drag in a stale, BOM-mismatched
        // firebase-auth transitive and risk duplicate KTX extension declarations.
        // Gradle's substitution API requires an explicit target version; "24.2.0" is
        // firebase-bom 34.19.0's own managed firebase-auth version (verified against its
        // published POM), so this substitution resolves in lockstep with the BOM rather
        // than against it. Re-verify this literal alongside any future firebaseBom bump.
        substitute(module("com.google.firebase:firebase-auth-ktx"))
            .using(module("com.google.firebase:firebase-auth:24.2.0"))
            .because("firebase-bom 34.x no longer manages firebase-auth-ktx; redirect to the merged firebase-auth artifact (transitively required by openfeedback-viewmodel -> dev.gitlive:firebase-auth)")
    }
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
        versionCode = 37
        versionName = "2025.10.00"
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

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/AL2.0")
            excludes.add("/META-INF/LGPL2.1")
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.Experimental",
        )
    }
}

dependencies {
    detektPlugins(libs.detekt.fomatting)

    implementation(project(":shared"))
    implementation(project(":core:analytics"))

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
