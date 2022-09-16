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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.BookmarksViewModel
import com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.androidapp.ui.theme.bookmarked
import com.gdgnantes.devfest.model.Session
import io.openfeedback.android.OpenFeedback

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel,
    openFeedback: OpenFeedback,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    val sessionState = viewModel.session.collectAsState()
    SessionLayout(
        modifier = modifier,
        sessionState = sessionState,
        openFeedback = openFeedback,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick
    )
}

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    sessionState: State<Session?>,
    openFeedback: OpenFeedback,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    sessionState.value?.let { session ->
        SessionLayout(
            openFeedback = openFeedback,
            modifier = modifier,
            session = session,
            onBackClick = onBackClick,
            onSocialLinkClick = onSocialLinkClick
        )
    }
}

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    session: Session,
    bookmarksViewModel: BookmarksViewModel = hiltViewModel(),
    openFeedback: OpenFeedback,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit,
) {
    SessionLayout(
        modifier = modifier,
        session = session,
        isBookmarked = bookmarksViewModel.subscribe(session.id).collectAsState(initial = false),
        openFeedback = openFeedback,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick,
        onSessionBookmarkClick = { isBookmarked ->
            bookmarksViewModel.setBookmarked(session.id, isBookmarked)
        }
    )
}

@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    session: Session,
    isBookmarked: State<Boolean>,
    openFeedback: OpenFeedback,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit,
    onSessionBookmarkClick: ((Boolean) -> Unit)
) {
    SessionLayout(
        modifier = modifier,
        session = session,
        isBookmarked = isBookmarked.value,
        openFeedback = openFeedback,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick,
        onSessionBookmarkClick = onSessionBookmarkClick
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLayout(
    modifier: Modifier = Modifier,
    session: Session,
    isBookmarked: Boolean,
    openFeedback: OpenFeedback,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit,
    onSessionBookmarkClick: ((Boolean) -> Unit)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = session.title,
                modifier = Modifier.testTag("topAppBar"),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Back"
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
            SessionDetails(
                modifier = Modifier.padding(8.dp),
                session = session,
                onSocialLinkClick = onSocialLinkClick
            )

            session.openFeedbackFormId?.let { openFeedbackFormId ->
                FeedbackForm(
                    openFeedback = openFeedback,
                    openFeedbackFormId = openFeedbackFormId,
                )
            }
        }
    }
}