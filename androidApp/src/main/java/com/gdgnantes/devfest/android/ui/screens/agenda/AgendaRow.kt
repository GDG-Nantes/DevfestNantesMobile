package com.gdgnantes.devfest.android.ui.screens.agenda

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.stubs.buildSessionStub

@Composable
fun AgendaRow(
    session: Session,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 4.dp
            )
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.h5
            )
            Row {
                Column(Modifier.weight(1F)) {
                    val speakers =
                        session.speakers.joinToString(", ") { it.getFullNameAndCompany() }
                    if (speakers.isNotBlank()) {
                        Text(
                            text = speakers,
                            style = MaterialTheme.typography.subtitle1,
                            //modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        Surface(
            modifier = Modifier
                .height(0.5.dp)
                .fillMaxWidth(),
            color = colors.onSurface
        ) {}
    }
}

@ExperimentalMaterial3Api
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun AgendaRowPreview() {
    DevFest_NantesTheme {
        Scaffold {
            AgendaRow(
                session = buildSessionStub()
            )
        }
    }
}