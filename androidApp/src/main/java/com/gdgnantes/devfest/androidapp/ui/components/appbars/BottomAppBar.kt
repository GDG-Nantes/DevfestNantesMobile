package com.gdgnantes.devfest.androidapp.ui.components.appbars

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.screens.Screen
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme

@Composable
fun BottomAppBar(
    selected: Screen?,
    modifier: Modifier = Modifier,
    onClick: (selected: Screen) -> Unit,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            selected = Screen.Agenda == selected,
            onClick = {
                onClick(Screen.Agenda)
            },
            icon = {
                Icon(
                    imageVector = Screen.Agenda.imageVector(Screen.Agenda == selected)!!,
                    contentDescription = stringResource(id = Screen.Agenda.title),
                    tint = iconColor(selected = Screen.Agenda == selected)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.screen_agenda),
                    color = labelColor(selected = Screen.Agenda == selected)
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = Screen.Venue == selected,
            onClick = {
                onClick(Screen.Venue)
            },
            icon = {
                Icon(
                    imageVector = Screen.Venue.imageVector(Screen.Venue == selected)!!,
                    contentDescription = stringResource(id = Screen.Venue.title),
                    tint = iconColor(selected = Screen.Venue == selected)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.screen_venue),
                    color = labelColor(selected = Screen.Venue == selected)
                )
            },
            alwaysShowLabel = true
        )

        NavigationBarItem(
            selected = Screen.About == selected,
            onClick = {
                onClick(Screen.About)
            },
            icon = {
                Icon(
                    imageVector = Screen.About.imageVector(Screen.About == selected)!!,
                    contentDescription = stringResource(id = Screen.About.title),
                    tint = iconColor(selected = Screen.About == selected)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.screen_about),
                    color = labelColor(selected = Screen.About == selected)
                )
            },
            alwaysShowLabel = true
        )
    }
}

@Composable
fun iconColor(selected: Boolean): Color =
    if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun labelColor(selected: Boolean): Color =
    if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

@Preview
@Composable
fun BottomAppBarPreview() {
    DevFestNantesTheme {
        BottomAppBar(
            selected = null,
            onClick = {}
        )
    }
}
