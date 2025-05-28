package com.gdgnantes.devfest.androidapp.ui.screens.session

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.utils.getDateFromIso8601
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.WebLinks
import java.text.SimpleDateFormat

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
