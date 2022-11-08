package com.gdgnantes.devfest.androidapp.ui.screens.about.partners

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.stubs.buildPartnerStub
import timber.log.Timber

@Composable
fun PartnerCard(
    modifier: Modifier = Modifier,
    partner: Partner,
    onClick: (Partner) -> Unit
) {
    partner.logoUrl?.let {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        ) {
            AsyncImage(
                modifier = Modifier
                    .padding(8.dp)
                    .height(50.dp)
                    .clickable { onClick(partner) },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(partner.logoUrl)
                    .crossfade(true)
                    .build(),
                onError = { state ->
                    Timber.w(
                        state.result.throwable,
                        "${partner.name}'s logo loading failed (url = ${partner.logoUrl})"
                    )
                },
                contentDescription = stringResource(
                    id = R.string.content_description_logo,
                    "${partner.name}"
                ),
                contentScale = ContentScale.Fit
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PartnerCardPreview() {
    DevFestNantesTheme {
        Scaffold {
            PartnerCard(
                modifier = Modifier.padding(it),
                partner = buildPartnerStub(),
                onClick = {})
        }
    }
}