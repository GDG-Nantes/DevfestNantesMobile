object Versions {
    const val kotlinVersion = "1.8.22"

    const val kotlinCoroutines = "1.6.4"
    const val kmpNativeCoroutines = "0.13.1"
    const val kotlinxDateTime = "0.4.0"

    const val googleServicesVersion = "4.3.13"
    const val crashlyticsGradleVersion = "2.9.1"

    const val accompanistPager = "0.24.13-rc"

    const val composeBom = "2023.06.01"
    const val composeCompiler = "1.4.8"
    const val navCompose = "2.6.0"
    const val accompanist = "0.24.13-rc"

    const val daggerVersion = "2.46.1"
    const val hiltAndroidXVersion = "1.0.0"

    const val multiplatformSettings = "0.8.1"

    const val junit = "4.13.2"
    const val androidXTestVersion = "1.5.0"
    const val androidXTestRunnerVersion = "1.5.2"
    const val androidXJUnitVersion = "1.1.5"
    const val espresso = "3.5.0"
}


object AndroidSdk {
    const val min = 23
    const val compile = 33
    const val target = compile
}

object Apollo {
    const val apolloVersion = "3.8.2"
    const val apolloRuntime = "com.apollographql.apollo3:apollo-runtime:$apolloVersion"
    const val apolloNormalizedCache =
        "com.apollographql.apollo3:apollo-normalized-cache:$apolloVersion"
    const val apolloNormalizedCacheSqlite =
        "com.apollographql.apollo3:apollo-normalized-cache-sqlite:$apolloVersion"
}

object Deps {
    const val material = "com.google.android.material:material:1.6.1"
    const val multiplatformSettings =
        "com.russhwolf:multiplatform-settings:${Versions.multiplatformSettings}"
    const val multiplatformSettingsCoroutines =
        "com.russhwolf:multiplatform-settings-coroutines:${Versions.multiplatformSettings}"
    const val openFeedback = "io.openfeedback:feedback-android-sdk-ui:0.0.7"
    const val timber = "com.jakewharton.timber:timber:5.0.1"
}

object Kotlinx {
    const val coroutinesCore =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinxDateTime}"
    const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
}

object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
}

object AndroidX {
    const val browser = "androidx.browser:browser:1.4.0"
    const val preference = "androidx.preference:preference:1.1.1"
    const val splashScreenCompat = "androidx.core:core-splashscreen:1.0.0"
}

object Accompanist {
    const val pager = "com.google.accompanist:accompanist-pager:${Versions.accompanistPager}"
    const val pagerIndicator =
        "com.google.accompanist:accompanist-pager-indicators:${Versions.accompanistPager}"
    const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:0.25.0"
    const val swipeRefresh =
        "com.google.accompanist:accompanist-swiperefresh:${Versions.accompanistPager}"
}

object Compose {
    const val composeBom = "androidx.compose:compose-bom:${Versions.composeBom}"
    const val activity = "androidx.activity:activity-compose:1.5.0"
    const val compiler = "androidx.compose.compiler:compiler:${Versions.composeCompiler}"
    const val ui = "androidx.compose.ui:ui"
    const val runtime = "androidx.compose.runtime:runtime"
    const val uiGraphics = "androidx.compose.ui:ui-graphics"
    const val uiTooling = "androidx.compose.ui:ui-tooling"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout"
    const val material = "androidx.compose.material:material"
    const val material3 = "androidx.compose.material3:material3:1.0.0-beta02"
    const val materialIconsCore =
        "androidx.compose.material:material-icons-core"
    const val materialIconsExtended = "androidx.compose.material:material-icons-extended"
    const val navigation = "androidx.navigation:navigation-compose:${Versions.navCompose}"
    const val coilCompose = "io.coil-kt:coil-compose:2.1.0"
    const val hiltNavigation =
        "androidx.hilt:hilt-navigation-compose:${Versions.hiltAndroidXVersion}"

}

object Dagger {
    const val hilt = "com.google.dagger:hilt-android:${Versions.daggerVersion}"
    const val compiler = "com.google.dagger:hilt-compiler:${Versions.daggerVersion}"
    const val androidTesting = "com.google.dagger:hilt-android-testing:${Versions.daggerVersion}"
}

object Firebase {
    const val firebaseBom = "com.google.firebase:firebase-bom:30.4.1"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx"
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    const val firebaseConfig = "com.google.firebase:firebase-config-ktx"

}

object Tests {
    const val junit = "junit:junit:${Versions.junit}"
    const val coroutinesTest =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
    const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"

    object Compose {
        const val uiTestComposeJUnit = "androidx.compose.ui:ui-test-junit4"
        const val uiTestComposeManifest = "androidx.compose.ui:ui-test-junit4"
    }
}

object AndroidTests {
    const val androidXTestCoreKtx = "androidx.test:core-ktx:${Versions.androidXTestVersion}"
    const val androidXTestRunner = "androidx.test:runner:${Versions.androidXTestRunnerVersion}"
    const val androidXTestRules = "androidx.test:rules:${Versions.androidXTestVersion}"
    const val androidXTestJUnitKtx = "androidx.test.ext:junit-ktx:${Versions.androidXJUnitVersion}"
    const val androidXTestTruth = "androidx.test.ext:truth:${Versions.androidXTestVersion}"
    const val androidXTestEspressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val androidXTestEspressoContrib =
        "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
    const val androidXTestEspressoIntents =
        "androidx.test.espresso:espresso-intents:${Versions.espresso}"

    object Compose {
        const val uiTestComposeJUnit = "androidx.compose.ui:ui-test-junit4"
    }
}