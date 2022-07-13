package com.gdgnantes.devfest.android.ui.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.Event
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdgnantes.devfest.android.R

sealed class Screen(
    val route: String,
    @StringRes val title: Int,
    val imageVectorFilled: ImageVector?,
    val imageVectorOutlined: ImageVector?,
) {
    fun imageVector(selected: Boolean) = if (selected) imageVectorFilled else imageVectorOutlined

    object Home : Screen(
        route = "home",
        title = R.string.screen_home,
        imageVectorFilled = null,
        imageVectorOutlined = null
    )

    object Agenda : Screen(
        route = "agenda",
        title = R.string.screen_agenda,
        imageVectorFilled = Icons.Filled.Event,
        imageVectorOutlined = Icons.Outlined.Event,
    )

    companion object {
        fun screenFromRoute(route: String) = when (route) {
            Home.route -> Home
            Agenda.route -> Agenda
            else -> throw Exception("Unknown route specified")
        }
    }
}
