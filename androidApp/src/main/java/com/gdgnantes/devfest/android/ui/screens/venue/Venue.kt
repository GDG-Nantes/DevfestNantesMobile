package com.gdgnantes.devfest.android.ui.screens.venue

import android.location.Location
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.model.Venue

@Composable
fun Venue(
    modifier: Modifier = Modifier,
    viewModel: VenueViewModel = hiltViewModel(),
    onNavigationClick: (Location) -> Unit
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
    onNavigationClick: (Location) -> Unit
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
    onNavigationClick: (Location) -> Unit
) {
    when (uiState) {
        UiState.SUCCESS ->
            Button(onClick = {
                val location = Location("unset").apply {
                    latitude = venue.latitude
                    longitude = venue.longitude
                }
                onNavigationClick(location)
            }) {
                Text("Open navigation")
            }
        else -> {}
    }
}