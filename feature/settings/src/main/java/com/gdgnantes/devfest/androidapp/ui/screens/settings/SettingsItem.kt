package com.gdgnantes.devfest.androidapp.ui.screens.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val subtitleComposable: (@Composable () -> Unit)? =
        subtitle?.run {
            @Composable {
                Text(text = this@run)
            }
        }
    SettingsItem(
        modifier = modifier,
        icon = icon,
        title = { Text(text = title) },
        subtitle = subtitleComposable,
        onClick = onClick,
    )
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
            Modifier
                .weight(1f)
                .clickable(onClick = { onClick?.invoke() }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SettingsTileIcon(icon = icon)
            SettingsTileTexts(title = title, subtitle = subtitle)
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
internal fun SettingsMenuLinkPreview() {
    MaterialTheme {
        SettingsItem(
            icon = { Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear") },
            title = "Hello",
            subtitle = "This is a longer text"
        )
    }
}
