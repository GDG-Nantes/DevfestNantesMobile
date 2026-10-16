package com.gdgnantes.devfest.feature.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gdgnantes.devfest.core.model.Partner
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.stubs.PartnerFixtures
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.about.di.aboutModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTestRule
import com.gdgnantes.devfest.core.ui.R as CoreUiR

@RunWith(AndroidJUnit4::class)
class AboutSmokeTest {

    @get:Rule(order = 0)
    val koinRule =
        KoinTestRule.create {
            modules(aboutModule, coreUiModule)
            modules(androidTestDoubles)
        }

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Test
    fun about_renders_a_fixture_partner_and_click_reports_it() {
        val partner = PartnerFixtures.byCategory.getValue(PartnerCategory.PXL).first()
        val logoDescription =
            InstrumentationRegistry.getInstrumentation().targetContext
                .getString(CoreUiR.string.content_description_logo, partner.name)
        var clicked: Partner? = null

        composeRule.setContent {
            AboutRoute(
                versionName = "test",
                versionCode = 1,
                onCodeOfConductClick = {},
                onDevFestNantesWebsiteClick = {},
                onFacebookClick = {},
                onTwitterClick = {},
                onLinkedInClick = {},
                onYouTubeClick = {},
                onPartnerClick = { clicked = it },
                onLocalCommunitiesClick = {},
                onGithubClick = {},
            )
        }

        // Partners arrive asynchronously from the fake store, after the first composition.
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithContentDescription(logoDescription).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(logoDescription).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithContentDescription(logoDescription).performClick()

        assertEquals(partner, clicked)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
