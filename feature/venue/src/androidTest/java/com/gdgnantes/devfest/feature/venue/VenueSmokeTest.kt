package com.gdgnantes.devfest.feature.venue

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.os.ConfigurationCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import coil.Coil
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.request.SuccessResult
import com.gdgnantes.devfest.core.domain.di.domainModule
import com.gdgnantes.devfest.core.domain.venue.VenueLanguageUseCase
import com.gdgnantes.devfest.core.model.stubs.buildVenueStub
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.venue.di.venueModule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTestRule

@RunWith(AndroidJUnit4::class)
class VenueSmokeTest {

    @get:Rule(order = 0)
    val koinRule =
        KoinTestRule.create {
            modules(venueModule, coreUiModule, domainModule)
            modules(androidTestDoubles)
        }

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * The floor plan control removes itself when its image fails to load. Serving every image from
     * memory keeps the test independent of the network.
     */
    @Before
    fun serveImagesInstantly() {
        val loader =
            ImageLoader.Builder(targetContext)
                .components { add(InstantImageInterceptor) }
                .build()
        Coil.setImageLoader(loader)
    }

    @After
    fun resetImageLoader() {
        Coil.reset()
    }

    @Test
    fun venue_renders_the_fixture_venue_and_floor_plan_click_reports_its_url() {
        val language =
            VenueLanguageUseCase()
                .languageFor(ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0]!!.language)
        val venue = buildVenueStub(language)
        var clickedUrl: String? = null

        composeRule.setContent {
            VenueRoute(onNavigationClick = {}, onVenuePlanClick = { clickedUrl = it })
        }

        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(venue.name)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(venue.name).assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(targetContext.getString(R.string.venue_plan_content_description))
            .performScrollTo()
            .performClick()

        assertEquals(venue.floorPlanUrl, clickedUrl)
    }

    private object InstantImageInterceptor : Interceptor {
        override suspend fun intercept(chain: Interceptor.Chain): ImageResult =
            SuccessResult(
                drawable = ColorDrawable(Color.LTGRAY),
                request = chain.request,
                dataSource = DataSource.MEMORY
            )
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
