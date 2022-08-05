@file:OptIn(ExperimentalMaterial3Api::class)

package com.gdgnantes.devfest.android.ui.screens.venue

import android.content.Context
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.android.utils.onNavigationClick
import com.gdgnantes.devfest.model.Venue
import com.gdgnantes.devfest.model.stubs.buildVenueStub
import timber.log.Timber

@Composable
fun VenueDetails(
    modifier: Modifier = Modifier,
    venue: Venue
) {
    Scaffold {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(it),
        ) {

            val context = LocalContext.current

            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(venue.imageUrl)
                    .crossfade(true)
                    .build(),
                onError = { state ->
                    Timber.w(state.result.throwable, "Poi's image loading failed")
                },
                contentDescription = "Place's logo",
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                Text(
                    text = venue.name,
                    style = MaterialTheme.typography.h5,
                )

                Text(
                    modifier = Modifier.clickable { onNavigationClick(context, venue) },
                    text = venue.address,
                    style = MaterialTheme.typography.subtitle2,
                )

                Button(onClick = {
                    onNavigationClick(context, venue)
                }) {
                    Text(stringResource(id = R.string.venue_go_to_button))
                }

                if (Locale.current.language.lowercase().contains("fr")) {
                    venue.descriptionFr
                } else {
                    venue.descriptionEn
                }.let { description ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = description,
                        style = MaterialTheme.typography.subtitle2,
                    )
                }
            }
        }
    }
}

private fun onNavigationClick(context: Context, venue: Venue) {
    val location = Location("unset").apply {
        latitude = venue.latitude
        longitude = venue.longitude
    }
    context.onNavigationClick(location)
}

@Preview
@Composable
fun VenueDetailsPreview() {
    DevFest_NantesTheme {
        VenueDetails(
            venue = buildVenueStub()
        )
    }
}