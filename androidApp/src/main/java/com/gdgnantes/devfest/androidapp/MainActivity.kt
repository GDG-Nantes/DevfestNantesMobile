package com.gdgnantes.devfest.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gdgnantes.devfest.androidapp.services.ExternalContentService
import com.gdgnantes.devfest.androidapp.ui.screens.Home
import com.gdgnantes.devfest.androidapp.ui.screens.Screen
import com.gdgnantes.devfest.androidapp.ui.screens.session.SessionLayout
import com.gdgnantes.devfest.androidapp.ui.screens.session.SessionViewModel
import com.gdgnantes.devfest.androidapp.ui.theme.DevFest_NantesTheme
import com.gdgnantes.devfest.androidapp.utils.assistedViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import io.openfeedback.android.OpenFeedback
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun sessionViewModelFactory(): SessionViewModel.SessionViewModelFactory
    }

    @Inject
    lateinit var openFeedback: OpenFeedback

    @Inject
    lateinit var externalContentService: ExternalContentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            DevFest_NantesTheme {
                val mainNavController = rememberNavController()

                NavHost(
                    navController = mainNavController,
                    startDestination = Screen.Home.route
                ) {
                    composable(route = Screen.Home.route) {
                        Home(
                            onSessionClick = { session ->
                                session.openFeedbackFormId?.let { mainNavController.navigate("${Screen.Session.route}/${session.id}") }
                            },
                            onWeblinkClick = { url ->
                                externalContentService.openUrl(url)
                            }
                        )
                    }

                    composable(
                        route = "${Screen.Session.route}/{sessionId}"
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments!!.getString("sessionId")!!
                        SessionLayout(
                            openFeedback = openFeedback,
                            viewModel = assistedViewModel {
                                SessionViewModel.provideFactory(
                                    sessionViewModelFactory(),
                                    sessionId
                                )
                            },
                            onBackClick = { mainNavController.popBackStack() },
                            onSocialLinkClick = { url ->
                                externalContentService.openUrl(url)
                            }
                        )
                    }
                }
            }
        }
    }
}