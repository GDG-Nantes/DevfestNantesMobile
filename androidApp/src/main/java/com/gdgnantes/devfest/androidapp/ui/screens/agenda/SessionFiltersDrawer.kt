package com.gdgnantes.devfest.androidapp.ui.screens.agenda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.utils.SessionFilter
import com.gdgnantes.devfest.model.Room

@Composable
fun AgendaFilterDrawer(
    rooms: Set<Room>,
    sessionFilters: Set<SessionFilter>,
    onSessionFiltersChanged: (Set<SessionFilter>) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HeaderItem(R.string.session_filters_drawer_title)
        FilterItem(
            text = stringResource(R.string.bookmarked),
            image = R.drawable.ic_bookmarked,
            checked = sessionFilters.any { it.type == SessionFilter.FilterType.BOOKMARK },
            onCheck = { checked ->
                val newSessionFilters = sessionFilters.toMutableSet().apply {
                    removeAll { it.type == SessionFilter.FilterType.BOOKMARK }
                    if (checked) add(SessionFilter(SessionFilter.FilterType.BOOKMARK, ""))
                }
                onSessionFiltersChanged(newSessionFilters)
            }
        )

        /*HeaderItem(R.string.session_filters_drawer_languages_label)
        val french = "French"
        FilterItem(
            text = stringResource(R.string.french),
            language = french,
            checked = sessionFilters.any { it.type == SessionFilter.FilterType.LANGUAGE && it.value == french },
            onCheck = { checked ->
                val newSessionFilters = sessionFilters.toMutableList().apply {
                    removeAll { it.type == SessionFilter.FilterType.LANGUAGE && it.value == french }
                    if (checked) add(SessionFilter(SessionFilter.FilterType.LANGUAGE, french))
                }
                onFiltersChanged(newSessionFilters)

            }
        )
        val english = "English"
        FilterItem(
            text = stringResource(R.string.english),
            language = english,
            checked = sessionFilters.any { it.type == SessionFilter.FilterType.LANGUAGE && it.value == english },
            onCheck = { checked ->
                val newSessionFilters = sessionFilters.toMutableList().apply {
                    removeAll { it.type == SessionFilter.FilterType.LANGUAGE && it.value == english }
                    if (checked) add(SessionFilter(SessionFilter.FilterType.LANGUAGE, english))
                }
                onFiltersChanged(newSessionFilters)
            }
        )*/

        HeaderItem(R.string.session_filters_drawer_rooms_label)
        for (room in rooms) {
            FilterItem(
                text = room.name,
                checked = sessionFilters.any { it.type == SessionFilter.FilterType.ROOM && it.value == room.id },
                onCheck = { checked ->
                    val newSessionFilters = sessionFilters.toMutableSet().apply {
                        removeAll { it.type == SessionFilter.FilterType.ROOM && it.value == room.id }
                        if (checked) add(SessionFilter(SessionFilter.FilterType.ROOM, room.id))
                    }
                    onSessionFiltersChanged(newSessionFilters)
                }
            )
        }
    }
}

@Composable
private fun FilterItem(
    text: String,
    image: Int? = null,
    language: String? = null,
    checked: Boolean,
    onCheck: (checked: Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
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
                painter = painterResource(image), contentDescription = text,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
        }
        /*if (language != null) {
            Text(
                modifier = Modifier.width(textLeftMargin),
                textAlign = TextAlign.Center,
                text = EmojiUtils.getLanguageInEmoji(language)!!
            )
        }*/
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (image == null && language == null) textLeftMargin else 0.dp),
            text = text,
            fontSize = 14.sp
        )
        Checkbox(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp), checked = checked, onCheckedChange = null
        )
    }
}

@Composable
private fun HeaderItem(text: Int) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        fontSize = 16.sp,
        text = stringResource(text)
    )
}