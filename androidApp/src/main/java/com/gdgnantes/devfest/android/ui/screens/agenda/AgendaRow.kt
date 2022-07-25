package com.gdgnantes.devfest.android.ui.screens.agenda

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.model.Session

@Composable
fun AgendaRow(
    session: Session,
    modifier: Modifier = Modifier
) {
    Text(text = session.title, modifier = modifier.padding(8.dp))
}