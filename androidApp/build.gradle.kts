plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    compileSdk = AndroidSdk.compile
    defaultConfig {
        applicationId = "com.gdgnantes.devfest.androidapp"
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
        versionCode = 2
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()

        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlin.Experimental",
            "-Xuse-experimental=kotlin.Experimental",
        )
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
    namespace = "com.gdgnantes.devfest.androidapp"
}

dependencies {
    implementation(project(":shared"))

    with(Kotlinx) {
        implementation(coroutinesCore)
    }

    with(Firebase) {
        implementation(platform(firebaseBom))
        implementation(firebaseAnalytics)
        implementation(firebaseCrashlytics)
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
