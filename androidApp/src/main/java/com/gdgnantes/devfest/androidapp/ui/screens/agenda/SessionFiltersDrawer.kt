package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.utils.SessionFilter
import com.gdgnantes.devfest.model.Complexity
import com.gdgnantes.devfest.model.Room
import com.gdgnantes.devfest.model.SessionLanguage
import com.gdgnantes.devfest.model.SessionType

@Composable
fun AgendaFilterDrawer(
    modifier: Modifier = Modifier,
    rooms: Set<Room>,
    sessionFilters: Set<SessionFilter>,
    onSessionFiltersChanged: (Set<SessionFilter>) -> Unit,
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        HeaderItem(
            modifier = modifier,
            text = R.string.session_filters_drawer_title
        )
        FilterItem(
            text = stringResource(R.string.bookmarked),
            image = R.drawable.ic_bookmarked,
            checked = sessionFilters.any { it.type == SessionFilter.FilterType.BOOKMARK },
            onCheck = { checked ->
                val newSessionFilters =
                    sessionFilters.toMutableSet().apply {
                        removeAll { it.type == SessionFilter.FilterType.BOOKMARK }
                        if (checked) add(SessionFilter(SessionFilter.FilterType.BOOKMARK, ""))
                    }
                onSessionFiltersChanged(newSessionFilters)
            }
        )

        FiltersLanguage(
            sessionFilters = sessionFilters,
            onSessionFiltersChanged = onSessionFiltersChanged
        )

        FiltersComplexity(
            sessionFilters = sessionFilters,
            onSessionFiltersChanged = onSessionFiltersChanged
        )

        HeaderItem(
            modifier = modifier,
            text = R.string.session_filters_drawer_rooms_label
        )
        for (room in rooms) {
            FilterItem(
                text = room.name,
                checked = sessionFilters.any { it.type == SessionFilter.FilterType.ROOM && it.value == room.id },
                onCheck = { checked ->
                    val newSessionFilters =
                        sessionFilters.toMutableSet().apply {
                            removeAll { it.type == SessionFilter.FilterType.ROOM && it.value == room.id }
                            if (checked) add(SessionFilter(SessionFilter.FilterType.ROOM, room.id))
                        }
                    onSessionFiltersChanged(newSessionFilters)
                }
            )
        }

        FiltersType(
            sessionFilters = sessionFilters,
            onSessionFiltersChanged = onSessionFiltersChanged
        )
        // Add spacing before the button
        Spacer(modifier = Modifier.height(24.dp))
        // Add the clear filters button
        Button(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            onClick = { onSessionFiltersChanged(emptySet()) }
        ) {
            Text(text = stringResource(id = R.string.session_filters_clear))
        }
    }
}

@Composable
private fun FiltersLanguage(
    modifier: Modifier = Modifier,
    sessionFilters: Set<SessionFilter>,
    onSessionFiltersChanged: (Set<SessionFilter>) -> Unit,
) {
    HeaderItem(
        modifier = modifier,
        text = R.string.session_filters_drawer_languages_label
    )

    val french = SessionLanguage.FRENCH.name
    FilterItem(
        modifier = modifier,
        text = stringResource(R.string.language_french),
        language = SessionLanguage.FRENCH,
        checked = sessionFilters.any { it.type == SessionFilter.FilterType.LANGUAGE && it.value == french },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet().apply {
                    removeAll { it.type == SessionFilter.FilterType.LANGUAGE && it.value == french }
                    if (checked) add(SessionFilter(SessionFilter.FilterType.LANGUAGE, french))
                }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
    val english = SessionLanguage.ENGLISH.name
    FilterItem(
        modifier = modifier,
        text = stringResource(R.string.language_english),
        language = SessionLanguage.ENGLISH,
        checked = sessionFilters.any { it.type == SessionFilter.FilterType.LANGUAGE && it.value == english },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet().apply {
                    removeAll { it.type == SessionFilter.FilterType.LANGUAGE && it.value == english }
                    if (checked) add(SessionFilter(SessionFilter.FilterType.LANGUAGE, english))
                }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
}

@Composable
private fun FiltersComplexity(
    modifier: Modifier = Modifier,
    sessionFilters: Set<SessionFilter>,
    onSessionFiltersChanged: (Set<SessionFilter>) -> Unit,
) {
    HeaderItem(
        modifier = modifier,
        text = R.string.session_filters_drawer_complexity_label
    )

    FilterItem(
        text = stringResource(R.string.complexity_beginner),
        checked =
        sessionFilters.any {
            it.type == SessionFilter.FilterType.COMPLEXITY &&
                    it.value == Complexity.BEGINNER.name
        },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet().apply {
                    removeAll { it.type == SessionFilter.FilterType.COMPLEXITY && it.value == Complexity.BEGINNER.name }
                    if (checked) {
                        add(
                            SessionFilter(
                                SessionFilter.FilterType.COMPLEXITY,
                                Complexity.BEGINNER.name
                            )
                        )
                    }
                }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
    FilterItem(
        text = stringResource(R.string.complexity_intermediate),
        checked =
        sessionFilters.any {
            it.type == SessionFilter.FilterType.COMPLEXITY && it.value == Complexity.INTERMEDIATE.name
        },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet().apply {
                    removeAll {
                        it.type == SessionFilter.FilterType.COMPLEXITY &&
                                it.value == Complexity.INTERMEDIATE.name
                    }
                    if (checked) {
                        add(
                            SessionFilter(
                                SessionFilter.FilterType.COMPLEXITY,
                                Complexity.INTERMEDIATE.name
                            )
                        )
                    }
                }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
    FilterItem(
        text = stringResource(R.string.complexity_advanced),
        checked =
        sessionFilters.any {
            it.type == SessionFilter.FilterType.COMPLEXITY &&
                    it.value == Complexity.ADVANCED.name
        },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet().apply {
                    removeAll { it.type == SessionFilter.FilterType.COMPLEXITY && it.value == Complexity.ADVANCED.name }
                    if (checked) {
                        add(
                            SessionFilter(
                                SessionFilter.FilterType.COMPLEXITY,
                                Complexity.ADVANCED.name
                            )
                        )
                    }
                }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
}

@Composable
private fun FiltersType(
    modifier: Modifier = Modifier,
    sessionFilters: Set<SessionFilter>,
    onSessionFiltersChanged: (Set<SessionFilter>) -> Unit,
) {
    HeaderItem(
        modifier = modifier,
        text = R.string.session_filters_drawer_type_label
    )

    FilterItem(
        text = stringResource(R.string.session_type_conference),
        checked =
        sessionFilters
            .any {
                it.type == SessionFilter.FilterType.TYPE &&
                        it.value == SessionType.CONFERENCE.name
            },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet()
                    .apply {
                        removeAll {
                            it.type == SessionFilter.FilterType.TYPE &&
                                    it.value == SessionType.CONFERENCE.name
                        }
                        if (checked) {
                            add(
                                SessionFilter(
                                    SessionFilter.FilterType.TYPE,
                                    SessionType.CONFERENCE.name
                                )
                            )
                        }
                    }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
    FilterItem(
        text = stringResource(R.string.session_type_quickie),
        checked =
        sessionFilters.any {
            it.type == SessionFilter.FilterType.TYPE &&
                    it.value == SessionType.QUICKIE.name
        },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet().apply {
                    removeAll { it.type == SessionFilter.FilterType.TYPE && it.value == SessionType.QUICKIE.name }
                    if (checked) {
                        add(
                            SessionFilter(
                                SessionFilter.FilterType.TYPE,
                                SessionType.QUICKIE.name
                            )
                        )
                    }
                }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
    FilterItem(
        text = stringResource(R.string.session_type_codelab),
        checked =
        sessionFilters.any {
            it.type == SessionFilter.FilterType.TYPE &&
                    it.value == SessionType.CODELAB.name
        },
        onCheck = { checked ->
            val newSessionFilters =
                sessionFilters.toMutableSet()
                    .apply {
                        removeAll {
                            it.type == SessionFilter.FilterType.TYPE &&
                                    it.value == SessionType.CODELAB.name
                        }
                        if (checked) {
                            add(
                                SessionFilter(
                                    SessionFilter.FilterType.TYPE,
                                    SessionType.CODELAB.name
                                )
                            )
                        }
                    }
            onSessionFiltersChanged(newSessionFilters)
        }
    )
}

@Composable
private fun FilterItem(
    modifier: Modifier = Modifier,
    text: String,
    image: Int? = null,
    language: SessionLanguage? = null,
    checked: Boolean,
    onCheck: (checked: Boolean) -> Unit,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .clickable {
                onCheck(!checked)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val textLeftMargin = 48.dp
        if (image != null) {
            Image(
                modifier = Modifier.width(textLeftMargin),
                painter = painterResource(image),
                contentDescription = text,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
        if (language != null) {
            val emoji =
                when (language) {
                    SessionLanguage.FRENCH -> "🇫🇷"
                    SessionLanguage.ENGLISH -> "🇬🇧"
                }

            Text(
                modifier = Modifier.width(textLeftMargin),
                textAlign = TextAlign.Center,
                text = emoji
            )
        }

        Text(
            modifier =
            Modifier
                .weight(1f)
                .padding(start = if (image == null && language == null) textLeftMargin else 0.dp),
            text = text,
            fontSize = 14.sp
        )
        Checkbox(
            modifier =
            Modifier
                .width(48.dp)
                .height(48.dp),
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
private fun HeaderItem(
    modifier: Modifier = Modifier,
    @StringRes text: Int
) {
    Text(
        modifier =
        modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        fontSize = 16.sp,
        text = stringResource(text)
    )
}
