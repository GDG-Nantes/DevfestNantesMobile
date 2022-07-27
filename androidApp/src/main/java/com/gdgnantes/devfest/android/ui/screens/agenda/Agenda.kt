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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.UiState
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.model.AgendaDay
import com.gdgnantes.devfest.model.stubs.buildSessionStub

@Composable
fun Agenda(
    viewModel: AgendaViewModel = hiltViewModel()
) {
    AgendaLayout(
        modifier = Modifier,
        uiState = viewModel.uiState.collectAsState(),
        days = viewModel.days.collectAsState(),
        onRefresh = viewModel::onRefresh
    )
}

@Composable
fun AgendaLayout(
    modifier: Modifier = Modifier,
    uiState: State<UiState>,
    days: State<Map<Int, AgendaDay>>,
    onRefresh: () -> Unit
) {
    AgendaLayout(
        modifier = modifier,
        uiState = uiState.value,
        days = days.value,
        onRefresh = onRefresh
    )
}

@Composable
fun AgendaLayout(
    modifier: Modifier = Modifier,
    uiState: UiState,
    days: Map<Int, AgendaDay>,
    onRefresh: () -> Unit

) {
    when (uiState) {
        UiState.LOADING -> {
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
                AgendaPager(initialPageIndex = 0,
                    days = days,
                    isRefreshing = true,
                    onRefresh = onRefresh,
                    onSessionClicked = { TODO() }
                )
            }
        }
        UiState.SUCCESS -> AgendaPager(initialPageIndex = 0,
            days = days,
            isRefreshing = false,
            onRefresh = onRefresh,
            onSessionClicked = { TODO() }
        )
        UiState.ERROR -> TODO()
    }
}

@ExperimentalMaterial3Api
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview
@Composable
fun AgendaLayoutPreview() {
    DevFest_NantesTheme {
        Scaffold {
            AgendaLayout(
                uiState = UiState.SUCCESS,
                days = mapOf(Pair(1, AgendaDay(1, listOf(buildSessionStub())))),
                onRefresh = {}
            )
        }
    }
}