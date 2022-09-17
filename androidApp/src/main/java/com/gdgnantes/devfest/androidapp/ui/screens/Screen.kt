package com.gdgnantes.devfest.androidapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdgnantes.devfest.androidapp.R

sealed class Screen(
    val route: String,
    @StringRes val title: Int,
    val imageVectorFilled: ImageVector?,
    val imageVectorOutlined: ImageVector?
) {
    fun imageVector(selected: Boolean) = if (selected) imageVectorFilled else imageVectorOutlined

    object About : Screen(
        route = "about",
        title = R.string.screen_about,
        imageVectorFilled = Icons.Filled.Info,
        imageVectorOutlined = Icons.Outlined.Info,
    )

    object Agenda : Screen(
        route = "agenda",
        title = R.string.screen_agenda,
        imageVectorFilled = Icons.Filled.Event,
        imageVectorOutlined = Icons.Outlined.Event,
    )

    object Home : Screen(
        route = "home",
        title = R.string.screen_home,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    object Session : Screen(
        route = "session",
        title = R.string.screen_session,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    object Settings : Screen(
        route = "settings",
        title = R.string.screen_settings,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    object Venue : Screen(
        route = "venue",
        title = R.string.screen_venue,
        imageVectorFilled = Icons.Filled.Domain,
        imageVectorOutlined = Icons.Outlined.Domain,
    )

    companion object {
        fun screenFromRoute(route: String) = when (route) {
            About.route -> About
            Agenda.route -> Agenda
            Home.route -> Home
            Venue.route -> Venue
            else -> throw Exception("Unknown route specified")
        }
    }
}
