package com.gdgnantes.devfest.androidapp.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.components.appbars.BottomAppBar
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.androidapp.ui.screens.about.About
import com.gdgnantes.devfest.androidapp.ui.screens.agenda.Agenda
import com.gdgnantes.devfest.androidapp.ui.screens.venue.Venue
import com.gdgnantes.devfest.model.Session
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.Agenda,
    onSessionClick: ((Session) -> Unit),
    onSettingsClick: () -> Unit,
    onWeblinkClick: (String) -> Unit
) {
    val homeNavController = rememberNavController()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val screen = currentDestination?.route?.run { Screen.screenFromRoute(this) } ?: startDestination

    val agendaFilterDrawerState = rememberDrawerState(DrawerValue.Closed)

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.testTag("topAppBar"),
                title = stringResource(id = screen.title),
                actions = {
                    when (screen) {
                        Screen.Agenda -> {
                            val scope = rememberCoroutineScope()
                            IconButton(onClick = {
                                scope.launch {
                                    if (agendaFilterDrawerState.isClosed) agendaFilterDrawerState.open() else agendaFilterDrawerState.close()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = stringResource(id = R.string.session_filters_action)
                                )

                            }
                        }
                        else -> {}
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(id = R.string.settings_action)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                selected = screen,
                onClick = {
                    homeNavController.navigate(it.route) {
                        popUpTo(homeNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) {
        NavHost(
            homeNavController,
            startDestination = Screen.Agenda.route,
            modifier = modifier.padding(it)
        ) {
            composable(Screen.Agenda.route) {
                Agenda(
                    agendaFilterDrawerState = agendaFilterDrawerState,
                    onSessionClick = onSessionClick
                )
            }

            composable(Screen.Venue.route) {
                Venue()
            }

            composable(Screen.About.route) {
                About(onWeblinkClick = onWeblinkClick)
            }
        }
    }
}