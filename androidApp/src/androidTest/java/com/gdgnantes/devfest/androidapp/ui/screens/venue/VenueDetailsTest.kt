package com.gdgnantes.devfest.androidapp.ui.screens.venue

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.model.ContentLanguage
import com.gdgnantes.devfest.model.stubs.buildVenueStub
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VenueDetailsTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun venueDetails_allElementsAreDisplayed() {
        val venue = buildVenueStub(language = ContentLanguage.ENGLISH)
        composeTestRule.setContent {
            VenueDetails(
                venue = venue,
                onNavigationClick = {},
                onVenuePlanClick = {}
            )
        }
        composeTestRule.onNodeWithText(venue.name).assertIsDisplayed()
        composeTestRule.onNodeWithText(venue.address).assertIsDisplayed()
        composeTestRule.onNodeWithText(venue.description).assertIsDisplayed()
    }

    @Test
    fun navigationButton_isDisplayed() {
        val venue = buildVenueStub(language = ContentLanguage.ENGLISH)
        composeTestRule.setContent {
            VenueDetails(
                venue = venue,
                onNavigationClick = {},
                onVenuePlanClick = {}
            )
        }
        val buttonText = context.getString(R.string.venue_go_to_button)
        composeTestRule.onNodeWithText(buttonText).assertIsDisplayed()
    }

    @Test
    fun venueImage_isDisplayed() {
        val venue = buildVenueStub(language = ContentLanguage.ENGLISH)
        composeTestRule.setContent {
            VenueDetails(
                venue = venue,
                onNavigationClick = {},
                onVenuePlanClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(
            context.getString(R.string.venue_image_content_description)
        ).assertIsDisplayed()
    }
}
