package com.gdgnantes.devfest.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gdgnantes.devfest.android.ui.screens.Home
import com.gdgnantes.devfest.android.ui.screens.Screen
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DevFest_NantesTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route
                ) {
                    composable(route = Screen.Home.route) {
                        Home()
                    }
                }
            }
        }
    }
}