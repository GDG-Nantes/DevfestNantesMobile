package com.gdgnantes.devfest.android.ui.components.appbars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdgnantes.devfest.android.ui.theme.DevFest_NantesTheme

@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (AppBarIcons.() -> Unit)? = null,
    actions: List<ActionItem> = emptyList(),
    onActionClicked: ((ActionItemId) -> Unit)? = null
) {
    SmallTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = navigationIcon.takeOrEmpty(),
        actions = {
            actions.forEach { action ->
                IconButton(onClick = { onActionClicked?.let { it(action.id) } }) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription?.let {
                            stringResource(id = it, action.formatArgs)
                        }
                    )
                }
            }
        }
    )
}

open class ActionItem(
    val id: ActionItemId,
    val icon: ImageVector,
    val contentDescription: Int?,
    val formatArgs: List<String> = emptyList()
)

sealed class ActionItemId {
    object FavoriteSchedulesActionItem : ActionItemId()
    object ShareActionItem : ActionItemId()
    object ReportActionItem : ActionItemId()
}

internal fun (@Composable AppBarIcons.() -> Unit)?.takeOrEmpty(): (@Composable () -> Unit) {
    if (this == null) return {}
    return {
        AppBarIcons.this()
    }
}

@Preview
@Composable
fun TopAppBarPreview() {
    DevFest_NantesTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TopAppBar(
                title = "Speakers"
            )
            TopAppBar(
                title = "Speakers",
                navigationIcon = { Back { } }
            )
        }
    }
}
