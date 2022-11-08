package com.gdgnantes.devfest.androidapp.ui.screens.about.partners

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.model.Partner

@Composable
fun Partners(
    modifier: Modifier = Modifier,
    viewModel: PartnersViewModel = hiltViewModel(),
    onPartnerClick: (Partner) -> Unit
) {
    Partners(
        modifier = modifier,
        platiniumPartners = viewModel.platiniumPartners.collectAsState(),
        goldPartners = viewModel.goldPartners.collectAsState(),
        virtualPartners = viewModel.virtualPartners.collectAsState(),
        onPartnerClick = onPartnerClick
    )
}

@Composable
fun Partners(
    modifier: Modifier = Modifier,
    platiniumPartners: State<List<Partner>>,
    goldPartners: State<List<Partner>>,
    virtualPartners: State<List<Partner>>,
    onPartnerClick: (Partner) -> Unit
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                R.string.partners_title,
            ),
            style = MaterialTheme.typography.titleMedium,
        )

        PartnersGroup(
            title = stringResource(R.string.partners_platinium_title),
            partners = platiniumPartners.value,
            onPartnerClick = onPartnerClick
        )

        PartnersGroup(
            title = stringResource(R.string.partners_gold_title),
            partners = goldPartners.value,
            onPartnerClick = onPartnerClick
        )

        PartnersGroup(
            title = stringResource(R.string.partners_virtual_title),
            partners = virtualPartners.value,
            onPartnerClick = onPartnerClick
        )
    }
}

@Composable
fun PartnersGroup(
    modifier: Modifier = Modifier,
    title: String,
    partners: List<Partner>,
    onPartnerClick: (Partner) -> Unit
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                partners.forEach { partner ->
                    PartnerCard(
                        partner = partner,
                        onClick = onPartnerClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PartnersPreview() {
    DevFestNantesTheme {
        Scaffold {
            Partners(modifier = Modifier.padding(it),
                onPartnerClick = {})
        }
    }
}
