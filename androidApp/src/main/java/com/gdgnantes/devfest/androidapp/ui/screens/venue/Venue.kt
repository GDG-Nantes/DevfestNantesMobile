package com.gdgnantes.devfest.androidapp.ui.screens.venue

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.ui.UiState
import com.gdgnantes.devfest.model.Venue

@Composable
fun Venue(
    modifier: Modifier = Modifier,
    viewModel: VenueViewModel = hiltViewModel(),
    onNavigationClick: () -> Unit,
) {
    VenueLayout(
        modifier = modifier,
        uiState = viewModel.uiState.collectAsState(),
        venue = viewModel.venue.collectAsState(),
        onNavigationClick = onNavigationClick
    )
}

@Composable
fun VenueLayout(
    modifier: Modifier = Modifier,
    uiState: State<UiState>,
    venue: State<Venue>,
    onNavigationClick: () -> Unit,
) {
    VenueLayout(
        modifier = modifier,
        uiState = uiState.value,
        venue = venue.value,
        onNavigationClick = onNavigationClick
    )
}

@Composable
fun VenueLayout(
    modifier: Modifier = Modifier,
    uiState: UiState,
    venue: Venue,
    onNavigationClick: () -> Unit,
) {
    when (uiState) {
        UiState.SUCCESS ->
            VenueDetails(
                modifier = modifier,
                venue = venue,
                onNavigationClick = onNavigationClick
            )
        else -> {}
    }
}