package com.gdgnantes.devfest.androidapp.ui.screens.venue.plan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gdgnantes.devfest.androidapp.R
import timber.log.Timber

@Composable
fun VenueFloorPlanButton(
    modifier: Modifier = Modifier,
    floorPlanUrl: String,
    onClick: () -> Unit,
) {
    var displayVenue by remember { mutableStateOf(true) }
    if (displayVenue) {
        OutlinedCard(
            modifier =
            modifier
                .fillMaxWidth()
                .height(250.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            onClick = onClick
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    modifier =
                    Modifier
                        .align(Alignment.Center),
                    model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(floorPlanUrl)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .crossfade(true)
                        .build(),
                    onError = { state ->
                        Timber.w(
                            state.result.throwable,
                            "Venue's floor plan loading failed"
                        )
                        displayVenue = false
                    },
                    contentDescription = stringResource(R.string.venue_plan_content_description),
                    contentScale = ContentScale.Fit
                )

                Icon(
                    modifier =
                    Modifier
                        .size(96.dp)
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    imageVector = Icons.Filled.ZoomIn,
                    contentDescription = null
                )
            }
        }
    }
}
