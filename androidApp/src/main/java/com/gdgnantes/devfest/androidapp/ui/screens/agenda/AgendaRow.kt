package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.BookmarksViewModel
import com.gdgnantes.devfest.androidapp.ui.components.SessionCategory
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.androidapp.ui.theme.bookmarked
import com.gdgnantes.devfest.androidapp.utils.isService
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionType
import com.gdgnantes.devfest.model.stubs.buildSessionStub
import com.gdgnantes.devfest.utils.getDurationAndLanguageString
import com.gdgnantes.devfest.utils.getDurationString

@Composable
fun AgendaRow(
    modifier: Modifier = Modifier,
    bookmarksViewModel: BookmarksViewModel = hiltViewModel(),
    session: Session,
    onSessionClick: ((Session) -> Unit)
) {
    AgendaRow(
        modifier = modifier,
        isBookmarked = bookmarksViewModel.subscribe(session.id).collectAsState(initial = false),
        session = session,
        onSessionClick = onSessionClick,
        onSessionBookmarkClick = { isBookmarked ->
            bookmarksViewModel.setBookmarked(session.id, isBookmarked, AnalyticsPage.AGENDA)
        }
    )
}

@Composable
fun AgendaRow(
    modifier: Modifier = Modifier,
    isBookmarked: State<Boolean>,
    session: Session,
    onSessionClick: ((Session) -> Unit),
    onSessionBookmarkClick: ((Boolean) -> Unit)
) {
    AgendaRow(
        modifier = modifier,
        isBookmarked = isBookmarked.value,
        session = session,
        onSessionClick = onSessionClick,
        onSessionBookmarkClick = onSessionBookmarkClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaRow(
    modifier: Modifier = Modifier,
    isBookmarked: Boolean,
    session: Session,
    onSessionClick: ((Session) -> Unit),
    onSessionBookmarkClick: ((Boolean) -> Unit)
) {
    ElevatedCard(
        modifier = modifier.padding(8.dp),
        onClick = { onSessionClick(session) }
    ) {
        Column {
            Column(
                modifier =
                Modifier.padding(
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

                    val duration =
                        when (session.type) {
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
                    if (session.type?.isService() == false) {
                        Box {
                            val icon =
                                if (isBookmarked) R.drawable.ic_bookmarked else R.drawable.ic_bookmark

                            val tint by animateColorAsState(
                                if (isBookmarked) {
                                    bookmarked
                                } else {
                                    Color.LightGray
                                },
                                label = "bookmarkAnimatedColorState"
                            )

                            IconToggleButton(
                                checked = isBookmarked,
                                onCheckedChange = {
                                    onSessionBookmarkClick(it)
                                },
                            ) {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = "favorite",
                                    tint = tint
                                )
                            }
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
fun AgendaRowPreview() {
    DevFestNantesTheme {
        AgendaRow(
            isBookmarked = true,
            session = buildSessionStub(),
            onSessionClick = {},
            onSessionBookmarkClick = {}
        )
    }
}
