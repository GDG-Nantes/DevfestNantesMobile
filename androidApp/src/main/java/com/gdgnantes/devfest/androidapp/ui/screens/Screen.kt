package com.gdgnantes.devfest.androidapp.ui.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Groups
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

    data object About : Screen(
        route = "about",
        title = R.string.screen_about,
        imageVectorFilled = Icons.Filled.Info,
        imageVectorOutlined = Icons.Outlined.Info,
    )

    data object Agenda : Screen(
        route = "agenda",
        title = R.string.screen_agenda,
        imageVectorFilled = Icons.Filled.Event,
        imageVectorOutlined = Icons.Outlined.Event,
    )

    data object Home : Screen(
        route = "home",
        title = R.string.screen_home,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    data object Session : Screen(
        route = "session",
        title = R.string.screen_session,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    data object Settings : Screen(
        route = "settings",
        title = R.string.screen_settings,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    data object Speaker : Screen(
        route = "speaker",
        title = R.string.screen_speaker,
        imageVectorFilled = Icons.Filled.Groups,
        imageVectorOutlined = Icons.Outlined.Groups
    )

    data object Speakers : Screen(
        route = "speakers",
        title = R.string.screen_speakers,
        imageVectorFilled = Icons.Filled.Groups,
        imageVectorOutlined = Icons.Outlined.Groups
    )

    data object DataCollection : Screen(
        route = "datacollection",
        title = R.string.screen_data_collection,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    data object Legal : Screen(
        route = "legal",
        title = R.string.settings_legal,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    data object Venue : Screen(
        route = "venue",
        title = R.string.screen_venue,
        imageVectorFilled = Icons.Filled.Domain,
        imageVectorOutlined = Icons.Outlined.Domain,
    )

    companion object {
        class UnknownRouteException(route: String) : Exception("Unknown route specified: $route")

        fun screenFromRoute(route: String) =
            when (route) {
                About.route -> About
                Agenda.route -> Agenda
                Home.route -> Home
                Speakers.route -> Speakers
                Venue.route -> Venue
                else -> throw UnknownRouteException(route)
            }
    }
}
