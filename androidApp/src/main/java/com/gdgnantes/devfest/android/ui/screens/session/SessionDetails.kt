package com.gdgnantes.devfest.android.ui.screens.session

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.ui.components.SessionCategory
import com.gdgnantes.devfest.android.ui.components.SessionComplexity
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.android.utils.getFormattedRange
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.stubs.buildSessionStub
import com.gdgnantes.devfest.utils.getDurationAndLanguageString

@Composable
fun SessionDetails(
    modifier: Modifier = Modifier,
    session: Session,
    onSocialLinkClick: (String) -> Unit
) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            session.title,
            style = MaterialTheme.typography.headlineLarge
        )

        val dateRangeAndRoom =
            session.scheduleSlot.getFormattedRange(LocalContext.current) + ", " + session.room.name
        Text(
            text = dateRangeAndRoom,
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = session.getDurationAndLanguageString(),
            style = MaterialTheme.typography.labelMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            session.category?.let { category ->
                SessionCategory(category = category)
            }
            session.complexity?.let { complexity ->
                SessionComplexity(complexity = complexity)
            }
        }

        Text(
            modifier = modifier.fillMaxWidth(),
            text = session.abstract,
            style = MaterialTheme.typography.bodyMedium
        )

        session.speakers.forEach { speaker ->
            SpeakerDetails(
                speaker = speaker,
                onSocialLinkClick = onSocialLinkClick
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SessionDetailsPreview() {
    DevFest_NantesTheme {
        Scaffold {
            SessionDetails(session = buildSessionStub(), onSocialLinkClick = {})
        }
    }
}