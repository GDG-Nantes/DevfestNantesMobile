package com.gdgnantes.devfest.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.analytics.AnalyticsService
import com.gdgnantes.devfest.androidapp.services.ExternalContentService
import com.gdgnantes.devfest.androidapp.ui.screens.Home
import com.gdgnantes.devfest.androidapp.ui.screens.Screen
import com.gdgnantes.devfest.androidapp.ui.screens.datacollection.DataCollectionAgreementDialog
import com.gdgnantes.devfest.androidapp.ui.screens.datacollection.DataCollectionSettingsScreen
import com.gdgnantes.devfest.androidapp.ui.screens.legal.LegalScreen
import com.gdgnantes.devfest.androidapp.ui.screens.session.SessionLayout
import com.gdgnantes.devfest.androidapp.ui.screens.session.SessionViewModel
import com.gdgnantes.devfest.androidapp.ui.screens.settings.Settings
import com.gdgnantes.devfest.androidapp.ui.screens.speakers.details.SpeakerLayout
import com.gdgnantes.devfest.androidapp.ui.screens.speakers.SpeakerViewModel
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.androidapp.utils.assistedViewModel
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionType
import com.gdgnantes.devfest.model.WebLinks
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), NavController.OnDestinationChangedListener {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun sessionViewModelFactory(): SessionViewModel.SessionViewModelFactory
        fun speakerViewModelFactory(): SpeakerViewModel.SpeakerViewModelFactory
    }

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var externalContentService: ExternalContentService

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            DevFestNantesTheme {
                val mainNavController = rememberNavController()
                mainNavController.addOnDestinationChangedListener(this)
                navController = mainNavController

                NavHost(
                    navController = mainNavController,
                    startDestination = Screen.Home.route
                ) {
                    composable(route = Screen.Home.route) {
                        Home(
                            analyticsService = analyticsService,
                            externalContentService = externalContentService,
                            onSessionClick = this@MainActivity::onSessionClick,
                            onSpeakerClick = { speaker ->
                                mainNavController.navigate("${Screen.Speaker.route}/${speaker.id}")
                                analyticsService.eventSpeakerOpened(speaker.id)
                            },
                            onSettingsClick = { mainNavController.navigate(Screen.Settings.route) }
                        )
                    }

                    composable(
                        route = "${Screen.Session.route}/{sessionId}"
                    ) { backStackEntry ->
                        val sessionId = backStackEntry.arguments!!.getString("sessionId")!!
                        SessionLayout(
                            viewModel = assistedViewModel {
                                SessionViewModel.provideFactory(
                                    sessionViewModelFactory(),
                                    sessionId
                                )
                            },
                            onBackClick = { mainNavController.popBackStack() },
                            onSocialLinkClick = { socialItem, speaker ->
                                socialItem.link?.let { link ->
                                    externalContentService.openUrl(link)
                                    analyticsService.eventSpeakerSocialLinkOpened(
                                        speaker.id,
                                        socialItem.type
                                    )
                                }
                            }
                        )
                    }

                    composable(
                        route = "${Screen.Speaker.route}/{speakerId}"
                    ) { backStackEntry ->
                        val speakerId = backStackEntry.arguments!!.getString("speakerId")!!
                        SpeakerLayout(
                            viewModel = assistedViewModel {
                                SpeakerViewModel.provideFactory(
                                    speakerViewModelFactory(),
                                    speakerId
                                )
                            },
                            onBackClick = { mainNavController.popBackStack() },
                            onSessionClick = this@MainActivity::onSessionClick,
                            onSocialLinkClick = { socialItem, speaker ->
                                socialItem.link?.let { link ->
                                    externalContentService.openUrl(link)
                                    analyticsService.eventSpeakerSocialLinkOpened(
                                        speaker.id,
                                        socialItem.type
                                    )
                                }
                            }
                        )
                    }

                    composable(
                        route = Screen.Settings.route
                    ) {
                        Settings(
                            onBackClick = { mainNavController.popBackStack() },
                            onLegalClick = { mainNavController.navigate(Screen.Legal.route) },
                            onOpenDataSharing = { mainNavController.navigate(Screen.DataCollection.route) },
                            onSupportClick = {
                                externalContentService.openUrl(WebLinks.SUPPORT.url)
                                analyticsService.eventLinkSupportOpened()
                            }
                        )
                    }

                    composable(
                        route = Screen.DataCollection.route
                    ) {
                        DataCollectionSettingsScreen(
                            onBackClick = { mainNavController.popBackStack() }
                        )
                    }

                    composable(
                        route = Screen.Legal.route
                    ) {
                        LegalScreen(
                            onBackClick = { mainNavController.popBackStack() }
                        )
                    }
                }

                DataCollectionAgreementDialog {
                    mainNavController.navigate(Screen.DataCollection.route)
                }
            }
        }
    }

    private fun onSessionClick(session: Session) {
        when (session.type) {
            SessionType.QUICKIE,
            SessionType.CONFERENCE,
            SessionType.CODELAB -> {
                navController.navigate("${Screen.Session.route}/${session.id}")
            }

            else -> {}
        }
        analyticsService.eventSessionOpened(session.id)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        destination.route?.let { route ->
            if (route == Screen.Home.route) {
                analyticsService.pageEvent(
                    AnalyticsPage.DATASHARING,
                    route
                )
            } else if (route == Screen.Legal.route) {
                analyticsService.pageEvent(AnalyticsPage.LEGAL, route)
            } else if (route.contains(Screen.Session.route)) {
                analyticsService.pageEvent(
                    AnalyticsPage.SESSION_DETAILS,
                    route
                )
            } else if (route == Screen.Settings.route) {
                analyticsService.pageEvent(AnalyticsPage.SETTINGS, route)
            } else if (route == Screen.DataCollection.route) {
                analyticsService.pageEvent(AnalyticsPage.DATASHARING, route)
            } else if (route == Screen.Speaker.route) {
                analyticsService.pageEvent(AnalyticsPage.SPEAKER, route)
            }
        }
    }
}