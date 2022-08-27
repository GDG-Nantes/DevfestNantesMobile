package com.gdgnantes.devfest.android.ui.screens.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(
    modifier: Modifier = Modifier,
    onWeblinkClick: (String) -> Unit
) {
    Scaffold {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(it)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AboutHeader(modifier.padding(horizontal = 8.dp))

            AboutLinks(
                modifier.padding(horizontal = 8.dp),
                onWeblinkClick = onWeblinkClick
            )

            AboutSocial(
                modifier.padding(horizontal = 8.dp),
                onWeblinkClick = onWeblinkClick
            )

            AboutVersion(modifier.padding(horizontal = 8.dp))
        }
    }
}

@Preview
@Composable
fun AboutPreview() {
    DevFest_NantesTheme {
        About(onWeblinkClick = {})
    }
}