package com.gdgnantes.devfest.androidapp.ui.screens.speakers.details

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.ui.components.SessionCategory
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionType
import com.gdgnantes.devfest.model.stubs.buildSessionStub
import com.gdgnantes.devfest.utils.getDurationAndLanguageString
import com.gdgnantes.devfest.utils.getDurationString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerSession(
    modifier: Modifier = Modifier,
    session: Session,
    onSessionClick: ((Session) -> Unit),
) {
    Card(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        onClick = { onSessionClick(session) }
    ) {
        Column {
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 4.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    session.category?.let { category ->
                        SessionCategory(category = category)
                    }

                    val duration = when (session.type) {
                        SessionType.QUICKIE,
                        SessionType.CONFERENCE,
                        SessionType.CODELAB -> {
                            session.getDurationAndLanguageString()
                        }

                        else -> {
                            session.getDurationString()
                        }
                    }
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Row {
                    Column(Modifier.weight(1F)) {
                        session.room?.let {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        val speakers =
                            session.speakers.joinToString(", ") { it.name }
                        if (speakers.isNotBlank()) {
                            Text(
                                text = speakers,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(
    "Night mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SpeakerSessionPreview() {
    DevFestNantesTheme {
        SpeakerSession(
            session = buildSessionStub(),
            onSessionClick = {},
        )
    }
}