package com.gdgnantes.devfest.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.gdgnantes.devfest.androidapp.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    var activityTestRule = createAndroidComposeRule(MainActivity::class.java)

    @Test
    fun ensureHeaderIsDisplayed() {
        activityTestRule.waitForIdle() // Ensure Compose is idle before querying
        val toolbarTitle =
            activityTestRule.onNode(hasTestTag("topAppBar"), useUnmergedTree = true)
        toolbarTitle.assertIsDisplayed()
    }

}