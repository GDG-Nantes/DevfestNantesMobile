package com.gdgnantes.devfest.androidapp.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme
import com.gdgnantes.devfest.model.Category
import com.gdgnantes.devfest.model.SessionType

@Composable
fun SessionType(
    modifier: Modifier = Modifier,
    type: SessionType
) {
    when (type) {
        SessionType.CONFERENCE -> R.string.session_type_conference
        SessionType.QUICKIE -> R.string.session_type_quickie
        SessionType.CODELAB -> R.string.session_type_codelab
        else -> null
    }?.let { textResource ->
        OutlinedCard(
            modifier = modifier,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
            ),
            colors = if (isSystemInDarkTheme()) {
                cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            } else {
                cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            }
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(id = textResource),
                fontSize = 12.sp
            )
        }
    }
}

@Preview(
    "Night mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SessionTypePreview() {
    DevFestNantesTheme {
        SessionCategory(category = Category("mobile", "📱 Mobile & IoT"))
    }
}