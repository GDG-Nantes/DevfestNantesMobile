package com.gdgnantes.devfest.androidapp.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFest_NantesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutLocalCommunities(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
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
            OutlinedCard(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                onClick = onClick
            ) {
                Image(
                    modifier = Modifier
                        .size(250.dp, 157.dp),
                    painter = painterResource(id = R.drawable.local_communities_logo),
                    contentDescription = stringResource(
                        id = R.string.content_description_local_communities_logo
                    )
                )
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
            AboutLocalCommunities(modifier = Modifier.padding(it), onClick = {})
        }
    }
}
