package com.gdgnantes.devfest.feature.venue

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VenueRoute(
    modifier: Modifier = Modifier,
    onNavigationClick: () -> Unit,
    onVenuePlanClick: (String) -> Unit,
) {
    Venue(
        modifier = modifier,
        onNavigationClick = onNavigationClick,
        onVenuePlanClick = onVenuePlanClick,
    )
}
