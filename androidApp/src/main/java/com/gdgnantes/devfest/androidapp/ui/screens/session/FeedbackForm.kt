package com.gdgnantes.devfest.androidapp.ui.screens.session

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.BuildConfig
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.model.Session
import io.openfeedback.OpenFeedback

@Composable
fun FeedbackForm(
    modifier: Modifier = Modifier,
    feedbackFormViewModel: FeedbackFormViewModel = hiltViewModel(),
    session: Session,
    onFeedbackFormFallbackLinkClick: (String) -> Unit
) {
    val openFeedbackFormId = session.openFeedbackFormId

    if (openFeedbackFormId != null) {
        if (feedbackFormViewModel.isOpenFeedbackEnabled.collectAsState().value) {
            if (feedbackFormViewModel.isOpenfeedbackFallbackRequested.collectAsState().value) {
                FallbackFeedbackForm(
                    modifier = modifier,
                    session = session,
                    onFeedbackFormFallbackLinkClick = onFeedbackFormFallbackLinkClick
                )
            } else {
                OpenfeedbackForm(
                    modifier = modifier,
                    sessionId = openFeedbackFormId
                )
            }
        }
    }
}

@Composable
fun OpenfeedbackForm(
    modifier: Modifier = Modifier,
    sessionId: String,
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.session_feedback_label),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.headlineSmall
        )

        OpenFeedback(
            projectId = BuildConfig.OPEN_FEEDBACK_FIREBASE_PROJECT_ID,
            sessionId = sessionId
        )
    }
}
