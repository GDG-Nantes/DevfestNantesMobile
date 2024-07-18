package com.gdgnantes.devfest.androidapp.ui.screens.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.androidapp.ui.components.LoadingLayout
import com.gdgnantes.devfest.model.Venue

@Composable
fun Venue(
    modifier: Modifier = Modifier,
    viewModel: VenueViewModel = hiltViewModel(),
    onNavigationClick: () -> Unit,
    onVenuePlanClick: (String) -> Unit,
) {
    VenueLayout(
        modifier = modifier,
        uiState = viewModel.uiState.collectAsState(),
        venue = viewModel.venue.collectAsState(),
        onNavigationClick = onNavigationClick,
        onVenuePlanClick = onVenuePlanClick
    )
}

@Composable
fun VenueLayout(
    modifier: Modifier = Modifier,
    uiState: State<UiState>,
    venue: State<Venue?>,
    onNavigationClick: () -> Unit,
    onVenuePlanClick: (String) -> Unit,
) {
    VenueLayout(
        modifier = modifier,
        uiState = uiState.value,
        venue = venue.value,
        onNavigationClick = onNavigationClick,
        onVenuePlanClick = onVenuePlanClick
    )
}

@Composable
fun VenueLayout(
    modifier: Modifier = Modifier,
    uiState: UiState,
    venue: Venue?,
    onNavigationClick: () -> Unit,
    onVenuePlanClick: (String) -> Unit,
) {
    if (uiState != UiState.LOADING && venue != null) {
        VenueDetails(
            modifier = modifier,
            venue = venue,
            onNavigationClick = onNavigationClick,
            onVenuePlanClick = { venue.floorPlanUrl?.let { onVenuePlanClick(it) } },
        )
    } else {
        LoadingLayout(modifier = modifier)
    }
}
