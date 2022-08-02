package com.gdgnantes.devfest.android.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gdgnantes.devfest.android.ui.components.appbars.BottomAppBar
import com.gdgnantes.devfest.android.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.android.ui.screens.agenda.Agenda
import com.gdgnantes.devfest.model.Session

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    modifier: Modifier = Modifier,
    startDestination: Screen = Screen.Agenda,
    onSessionClick: ((Session) -> Unit)
) {
    val homeNavController = rememberNavController()
    val navBackStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val screen = currentDestination?.route?.run { Screen.screenFromRoute(this) } ?: startDestination
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(id = screen.title),
                modifier = Modifier.testTag("topAppBar")
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
                    onSessionClick = onSessionClick
                )
            }

            composable(Screen.Venue.route) {
                Venue()
            }

            composable(Screen.About.route) {
                About()
            }
        }
    }
}