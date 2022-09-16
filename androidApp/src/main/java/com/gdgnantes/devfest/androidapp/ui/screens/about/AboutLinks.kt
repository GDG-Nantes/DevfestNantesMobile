package com.gdgnantes.devfest.androidapp.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.WebLinks
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFest_NantesTheme

@Composable
fun AboutLinks(
    modifier: Modifier = Modifier,
    onWeblinkClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        OutlinedButton(onClick = { onWeblinkClick(WebLinks.CODE_OF_CONDUCT.url) }) {
            Text(text = stringResource(id = R.string.about_code_of_conduct))
        }

        OutlinedButton(onClick = { onWeblinkClick(WebLinks.WEBSITE.url) }) {
            Text(text = stringResource(id = R.string.about_website))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AboutLinksPreview() {
    DevFest_NantesTheme {
        Scaffold {
            AboutLinks(modifier = Modifier.padding(it), onWeblinkClick = {})
        }
    }
}
