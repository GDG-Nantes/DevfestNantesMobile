package com.gdgnantes.devfest.androidapp.ui.screens.about

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme

@Composable
fun AboutHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = R.drawable.about_header),
                contentDescription = stringResource(id = R.string.content_description_about_header)
            )
        }

        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(id = R.string.screen_about_header_body)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun AboutHeaderPreview() {
    DevFestNantesTheme {
        Scaffold {
            AboutHeader(modifier = Modifier.padding(it))
        }
    }
}