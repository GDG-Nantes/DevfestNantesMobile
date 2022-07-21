package com.gdgnantes.devfest.android.ui.screens.agenda

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme

@Composable
fun Agenda(
    modifier: Modifier = Modifier
) {
    val agendaViewModel = hiltViewModel<AgendaViewModel>()
    val daysState = agendaViewModel.days.collectAsState()
    val days = daysState.value
    if (days.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator()
            Text(
                modifier = modifier,
                text = stringResource(id = R.string.agenda_empty)
            )
        }
    } else {
        AgendaPager(initialPageIndex = 1,
            days = days.keys.toList().map { key -> key.toString() })
    }
}

@ExperimentalMaterial3Api
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun AgendaPreview() {
    DevFest_NantesTheme {
        Scaffold {
            Agenda()
        }
    }
}