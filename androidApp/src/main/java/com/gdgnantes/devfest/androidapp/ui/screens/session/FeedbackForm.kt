package com.gdgnantes.devfest.androidapp.ui.screens.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.utils.getDateFromIso8601
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.WebLinks
import io.openfeedback.android.OpenFeedback
import io.openfeedback.android.components.SessionFeedbackContainer
import java.text.SimpleDateFormat
import java.util.Locale

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
                    openFeedback = feedbackFormViewModel.openFeedback,
                    openFeedbackFormId = openFeedbackFormId
                )
            }
        }
    }
}

@Composable
fun FallbackFeedbackForm(
    modifier: Modifier = Modifier,
    session: Session,
    onFeedbackFormFallbackLinkClick: (String) -> Unit,
) {
    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedButton(
            modifier = Modifier.align(Alignment.Center),
            onClick = {
                getFallbackUrlForSession(
                    session
                )?.run { onFeedbackFormFallbackLinkClick(this) }
            }
        ) {
            Text(
                text = stringResource(id = R.string.session_feedback_label)
            )
        }
    }
}

private fun getFallbackUrlForSession(session: Session): String? {
    val openFeedbackFormId = session.openFeedbackFormId ?: return null
    return getDateFromIso8601(session.scheduleSlot.startDate)?.run {
        val pattern = "yyyy-MM-dd"
        val simpleDateFormat = SimpleDateFormat(pattern)
        simpleDateFormat.format(this)
    }?.run { "${WebLinks.OPENFEEDBACK_BASE_URL.url}/$this/$openFeedbackFormId" }
}

@Composable
fun OpenfeedbackForm(
    modifier: Modifier = Modifier,
    openFeedback: OpenFeedback,
    openFeedbackFormId: String,
) {
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
            modifier =
            Modifier
                .padding(horizontal = 8.dp)
        )
    }
}
