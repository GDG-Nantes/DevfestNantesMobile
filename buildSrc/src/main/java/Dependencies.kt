object Versions {
    const val kotlinVersion = "1.7.0"

    const val kotlinCoroutines = "1.6.3"
    const val kmpNativeCoroutines = "0.12.5-new-mm"
    const val kotlinxDateTime = "0.4.0"

    const val accompanistPager = "0.24.13-rc"

    const val compose = "1.2.0-rc03"
    const val composeCompiler = "1.2.0"
    const val navCompose = "2.4.2"
    const val accompanist = "0.24.13-rc"

    const val daggerVersion = "2.42"
    const val hiltAndroidXVersion = "1.0.0"

    const val multiplatformSettings = "0.8.1"

    const val junit = "4.13"
    const val androidXTestVersion = "1.4.0"
    const val androidXJUnitVersion = "1.1.3"
    const val espresso = "3.3.0"
}


object AndroidSdk {
    const val min = 23
    const val compile = 32
    const val target = compile
}

object Deps {
    const val multiplatformSettings =
        "com.russhwolf:multiplatform-settings:${Versions.multiplatformSettings}"
    const val multiplatformSettingsCoroutines =
        "com.russhwolf:multiplatform-settings-coroutines:${Versions.multiplatformSettings}"
    const val openFeedback = "io.openfeedback:feedback-android-sdk-ui:0.0.7"
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

object Accompanist {
    const val pager = "com.google.accompanist:accompanist-pager:${Versions.accompanistPager}"
    const val pagerIndicator =
        "com.google.accompanist:accompanist-pager-indicators:${Versions.accompanistPager}"
    const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:0.25.0"
    const val swipeRefresh =
        "com.google.accompanist:accompanist-swiperefresh:${Versions.accompanistPager}"
}

object Compose {
    const val activity = "androidx.activity:activity-compose:1.5.0"
    const val compiler = "androidx.compose.compiler:compiler:${Versions.composeCompiler}"
    const val ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val runtime = "androidx.compose.runtime:runtime:${Versions.compose}"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
    const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val material = "androidx.compose.material:material:${Versions.compose}"
    const val material3 = "androidx.compose.material3:material3:1.0.0-alpha14"
    const val materialIconsCore =
        "androidx.compose.material:material-icons-core:${Versions.compose}"
    const val materialIconsExtended = "androidx.compose.material:material-icons-extended:${Versions.compose}"
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

object Tests {
    const val junit = "junit:junit:${Versions.junit}"
    const val coroutinesTest =
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}"
    const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"
}

object AndroidTests {
    const val androidXComposeUiTest = "androidx.compose.ui:ui-test-junit4:${Versions.compose}"
    const val androidXTestCoreKtx = "androidx.test:core-ktx:${Versions.androidXTestVersion}"
    const val androidXTestRunner = "androidx.test:runner:${Versions.androidXTestVersion}"
    const val androidXTestRules = "androidx.test:rules:${Versions.androidXTestVersion}"
    const val androidXTestJUnitKtx = "androidx.test.ext:junit-ktx:${Versions.androidXJUnitVersion}"
    const val androidXTestTruth = "androidx.test.ext:truth:${Versions.androidXTestVersion}"
    const val androidXTestEspressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val androidXTestEspressoContrib =
        "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
    const val androidXTestEspressoIntents =
        "androidx.test.espresso:espresso-intents:${Versions.espresso}"
}