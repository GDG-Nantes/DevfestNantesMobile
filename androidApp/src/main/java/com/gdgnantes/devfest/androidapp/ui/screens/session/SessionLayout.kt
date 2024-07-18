package com.gdgnantes.devfest.androidapp.ui.screens.session

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.BookmarksViewModel
import com.gdgnantes.devfest.androidapp.ui.components.LoadingLayout
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.androidapp.ui.theme.bookmarked
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SocialItem
import com.gdgnantes.devfest.model.Speaker

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel,
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit,
    onFeedbackFormFallbackLinkClick: (String) -> Unit
) {
    val sessionState = viewModel.session.collectAsState()
    SessionLayout(
        modifier = modifier,
        sessionState = sessionState,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick,
        onFeedbackFormFallbackLinkClick = onFeedbackFormFallbackLinkClick
    )
}

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    sessionState: State<Session?>,
    bookmarksViewModel: BookmarksViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit,
    onFeedbackFormFallbackLinkClick: (String) -> Unit
) {
    val session = sessionState.value
    val isBookmarkedState =
        session?.run { bookmarksViewModel.subscribe(session.id).collectAsState(initial = false) }
            ?: (
                    remember {
                        mutableStateOf(false)
                    }
                    )
    SessionLayout(
        modifier = modifier,
        session = session,
        isBookmarked = isBookmarkedState,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick,
        onSessionBookmarkClick = { isBookmarked ->
            session?.id?.let {
                bookmarksViewModel.setBookmarked(
                    it,
                    isBookmarked,
                    AnalyticsPage.SESSION_DETAILS
                )
            }
        },
        onFeedbackFormFallbackLinkClick = onFeedbackFormFallbackLinkClick
    )
}

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    session: Session?,
    isBookmarked: State<Boolean>,
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit,
    onSessionBookmarkClick: ((Boolean) -> Unit),
    onFeedbackFormFallbackLinkClick: (String) -> Unit
) {
    SessionLayout(
        modifier = modifier,
        session = session,
        isBookmarked = isBookmarked.value,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick,
        onSessionBookmarkClick = onSessionBookmarkClick,
        onFeedbackFormFallbackLinkClick = onFeedbackFormFallbackLinkClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    session: Session?,
    isBookmarked: Boolean,
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit,
    onSessionBookmarkClick: ((Boolean) -> Unit),
    onFeedbackFormFallbackLinkClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = session?.title ?: stringResource(id = R.string.placeholder_session_details),
                modifier = Modifier.testTag("topAppBar"),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = stringResource(id = R.string.action_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val containerColor by animateColorAsState(
                if (isBookmarked) Color.White else bookmarked
            )
            FloatingActionButton(
                containerColor = containerColor,
                onClick = {
                    onSessionBookmarkClick(!isBookmarked)
                }
            ) {
                Crossfade(isBookmarked) { isBookmarked ->
                    Image(
                        painterResource(
                            if (isBookmarked) R.drawable.ic_bookmarked_fab else R.drawable.ic_bookmark_fab
                        ),
                        contentDescription = stringResource(R.string.bookmarked)
                    )
                }
            }
        }
    ) {
        Column(
            modifier
                .verticalScroll(state = rememberScrollState())
                .padding(it)
        ) {
            if (session != null) {
                SessionDetails(
                    modifier = Modifier.padding(8.dp),
                    session = session,
                    onSocialLinkClick = onSocialLinkClick
                )

                FeedbackForm(
                    session = session,
                    onFeedbackFormFallbackLinkClick = onFeedbackFormFallbackLinkClick
                )
            } else {
                LoadingLayout()
            }
        }
    }
}
