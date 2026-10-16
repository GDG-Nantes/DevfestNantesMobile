package com.gdgnantes.devfest.feature.about.partners

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.stubs.PartnerFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PartnersViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val store = FakeDevFestNantesStore()

    @Test
    fun exposes_each_supported_category_from_the_fixtures() {
        val viewModel = PartnersViewModel(store)
        val fixtures = PartnerFixtures.byCategory

        assertEquals(fixtures.getValue(PartnerCategory.PXL), viewModel.exclusivePartners.value)
        assertEquals(fixtures.getValue(PartnerCategory.PLATINIUM), viewModel.platiniumPartners.value)
        assertEquals(fixtures.getValue(PartnerCategory.GOLD), viewModel.goldPartners.value)
        assertEquals(fixtures.getValue(PartnerCategory.VIRTUAL), viewModel.virtualPartners.value)
        assertEquals(fixtures.getValue(PartnerCategory.VELOTYPE), viewModel.velotypePartners.value)
    }

    // Characterizes current behavior: the PARTNERS category has no flow, so its partners are
    // never shown by the screen.
    @Test
    fun partners_category_is_not_exposed() {
        val viewModel = PartnersViewModel(store)

        val exposed =
            listOf(
                viewModel.exclusivePartners.value,
                viewModel.platiniumPartners.value,
                viewModel.goldPartners.value,
                viewModel.virtualPartners.value,
                viewModel.velotypePartners.value
            ).flatten()

        assertFalse(PartnerFixtures.communityHub in exposed)
        assertFalse(exposed.any { it.name == "Community Hub" })
    }

    @Test
    fun pushed_partners_replace_the_lists() =
        runTest {
            val viewModel = PartnersViewModel(store)

            viewModel.goldPartners.test {
                assertEquals(PartnerFixtures.byCategory.getValue(PartnerCategory.GOLD), awaitItem())

                store.partnersState.value = mapOf(PartnerCategory.GOLD to listOf(PartnerFixtures.remoteFirst))

                assertEquals(listOf(PartnerFixtures.remoteFirst), awaitItem())
            }

            assertEquals(emptyList(), viewModel.exclusivePartners.value)
            assertEquals(emptyList(), viewModel.platiniumPartners.value)
            assertEquals(emptyList(), viewModel.virtualPartners.value)
            assertEquals(emptyList(), viewModel.velotypePartners.value)
        }
}
