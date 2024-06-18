object Versions {

    const val junit = "4.13.2"
    const val androidXTestVersion = "1.5.0"
    const val androidXTestRunnerVersion = "1.5.2"
    const val androidXJUnitVersion = "1.1.5"
    const val espresso = "3.5.0"
}


object AndroidSdk {
    const val min = 23
    const val compile = 34
    const val target = compile
}

object Tests {
    const val junit = "junit:junit:${Versions.junit}"
    
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