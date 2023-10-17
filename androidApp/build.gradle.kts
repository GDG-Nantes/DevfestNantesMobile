import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.release.set(17)
}
tasks.withType(KotlinCompile::class.java).configureEach {
    (kotlinOptions as? KotlinJvmOptions)?.jvmTarget = "17"
}

android {
    compileSdk = AndroidSdk.compile
    defaultConfig {
        applicationId = "com.gdgnantes.devfest.androidapp"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
        versionCode = 20
        versionName = "2.0.5"
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
    namespace = "com.gdgnantes.devfest.androidapp"
}

dependencies {
    implementation(project(":shared"))

    with(Kotlinx) {
        implementation(coroutinesCore)
        implementation(dateTime)
    }

    with(Firebase) {
        implementation(platform(firebaseBom))
        implementation(firebaseAnalytics)
        implementation(firebaseCrashlytics)
        implementation(firebaseConfig)
    }

    with(Deps) {
        implementation(material)
        implementation(openFeedback)
        implementation(timber)
    }

    with(AndroidX) {
        implementation(browser)
        implementation(preference)
        implementation(splashScreenCompat)
    }

    with(Accompanist) {
        implementation(pager)
        implementation(pagerIndicator)
        implementation(systemUiController)
        implementation(swipeRefresh)
    }

    with(Compose) {
        implementation(platform(composeBom))
        implementation(activity)
        implementation(coilCompose)
        implementation(material3)
        implementation(materialIconsCore)
        implementation(materialIconsExtended)
        implementation(navigation)
        debugImplementation(uiTooling)
        implementation(uiToolingPreview)
        implementation(hiltNavigation)
    }

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

    with(Tests) {
        testImplementation(junit)
        testImplementation(coroutinesTest)
        testImplementation(kotlinJUnit)

        with(Tests.Compose) {
            // UI Tests
            androidTestImplementation(uiTestComposeJUnit)
            // Debug
            debugImplementation(uiTestComposeManifest)
        }
    }

    with(AndroidTests) {
        androidTestImplementation(androidXTestRules)
        androidTestImplementation(androidXTestJUnitKtx)
        androidTestImplementation(androidXTestTruth)
        androidTestImplementation(androidXTestEspressoCore)
        androidTestImplementation(androidXTestEspressoContrib)
        androidTestImplementation(androidXTestEspressoIntents)

        with(AndroidTests.Compose) {
            // UI Tests
            androidTestImplementation(uiTestComposeJUnit)
        }
    }
}
