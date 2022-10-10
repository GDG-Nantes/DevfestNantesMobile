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
import com.gdgnantes.devfest.androidapp.R
import io.openfeedback.android.OpenFeedback
import io.openfeedback.android.components.SessionFeedbackContainer
import java.util.*

@Composable
fun FeedbackForm(
    modifier: Modifier = Modifier,
    feedbackFormViewModel: FeedbackFormViewModel = hiltViewModel(),
    openFeedback: OpenFeedback,
    openFeedbackFormId: String
) {
    if (feedbackFormViewModel.isOpenFeedbackEnabled.collectAsState().value) {
        Column(
            modifier = modifier
        ) {
            Text(
                text = stringResource(id = R.string.session_feedback_label),
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.headlineSmall
            )

            SessionFeedbackContainer(
                openFeedback = openFeedback,
                sessionId = openFeedbackFormId,
                language = Locale.getDefault().language,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
        }
    }
}