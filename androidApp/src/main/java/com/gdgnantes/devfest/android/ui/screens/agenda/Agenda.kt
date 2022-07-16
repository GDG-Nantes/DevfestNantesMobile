package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Agenda() {
    val agendaViewModel = hiltViewModel<AgendaViewModel>()
    Text(text = "Loading...")
}