package com.gdgnantes.devfest.android.ui.screens.agenda

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme

@Composable
fun EmptyLayout(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(id = R.string.empty_day),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    "Night mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun EmptyLayoutlPreview() {
    DevFest_NantesTheme {
        Scaffold {
            EmptyLayout(modifier = Modifier.padding(it))
        }
    }
}
