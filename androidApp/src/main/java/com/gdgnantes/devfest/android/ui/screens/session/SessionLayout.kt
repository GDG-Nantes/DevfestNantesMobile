package com.gdgnantes.devfest.android.ui.screens.session

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.R
import com.gdgnantes.devfest.android.ui.components.appbars.TopAppBar
import com.gdgnantes.devfest.model.Session
import io.openfeedback.android.OpenFeedback
import io.openfeedback.android.components.SessionFeedbackContainer
import java.util.*

@Composable
fun SessionLayout(
    openFeedback: OpenFeedback,
    viewModel: SessionViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit
) {
    SessionLayout(
        openFeedback = openFeedback,
        sessionState = viewModel.session.collectAsState(),
        modifier = modifier,
        onBackClick = onBackClick,
        onSocialLinkClick = onSocialLinkClick
    )
}

@Composable
fun SessionLayout(
    openFeedback: OpenFeedback,
    sessionState: State<Session?>,
    modifier: Modifier = Modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionLayout(
    openFeedback: OpenFeedback,
    session: Session,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSocialLinkClick: (String) -> Unit
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
        }) {
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

            Text(
                text = stringResource(id = R.string.session_feedback_label),
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineSmall
            )

            SessionFeedbackContainer(
                openFeedback = openFeedback,
                sessionId = "173222", //session.id <-- Uncomment once DevFest Nantes' instance is setup.
                language = Locale.getDefault().language,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
        }

    }
}