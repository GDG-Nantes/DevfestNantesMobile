package com.gdgnantes.devfest.androidapp.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.WebLinks
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFest_NantesTheme

@Composable
fun AboutLocalCommunities(
    modifier: Modifier = Modifier,
    onWeblinkClick: (String) -> Unit
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(
                R.string.local_communities_title
            ),
            style = MaterialTheme.typography.titleMedium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(onClick = { onWeblinkClick(WebLinks.NANTES_TECH_COMMUNITIES.url) }) {
                Text(text = stringResource(id = R.string.local_communities_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AboutLocalCommunitiesPreview() {
    DevFest_NantesTheme {
        Scaffold {
            AboutLocalCommunities(modifier = Modifier.padding(it), onWeblinkClick = {})
        }
    }
}
