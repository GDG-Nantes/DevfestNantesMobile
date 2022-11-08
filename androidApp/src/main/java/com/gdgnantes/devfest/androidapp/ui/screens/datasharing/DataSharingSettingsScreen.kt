package com.gdgnantes.devfest.androidapp.ui.screens.datasharing

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.services.DataSharingService
import com.gdgnantes.devfest.androidapp.ui.screens.Screen
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme

@Composable
fun DataSharingSettingsScreen(
    viewModel: DataSharingViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    DataSharingSettingsScreen(
        dataSharingServicesActivationStatus = viewModel.dataSharingServicesActivationStatus.collectAsState(
            initial = emptyMap()
        ),
        onServiceActivationStatusChange = viewModel::onServiceActivationStatusChange,
        onAllServicesActivationStatusChange = viewModel::onAllServicesActivationStatusChange,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSharingSettingsScreen(
    modifier: Modifier = Modifier,
    dataSharingServicesActivationStatus: State<Map<DataSharingService, Boolean>>,
    onServiceActivationStatusChange: ((DataSharingService, Boolean) -> Unit),
    onAllServicesActivationStatusChange: ((Boolean) -> Unit),
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar(
                title = stringResource(id = Screen.DataSharing.title),
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
        }
    ) {
        Box(
            modifier = modifier
                .padding(it)
                .fillMaxHeight(),
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(
                        id = R.string.legal_data_sharing_body
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )

                AllDataSharingToolSwitch(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    dataSharingServicesActivationStatus,
                ) { checked ->
                    onAllServicesActivationStatusChange(checked)
                }

                DataSharingServices(
                    dataSharingServicesActivationStatus = dataSharingServicesActivationStatus,
                    onServiceActivationStatusChange = onServiceActivationStatusChange
                )
            }
        }
    }
}

@Composable
fun DataSharingServices(
    modifier: Modifier = Modifier,
    dataSharingServicesActivationStatus: State<Map<DataSharingService, Boolean>>,
    onServiceActivationStatusChange: ((DataSharingService, Boolean) -> Unit),
) {
    dataSharingServicesActivationStatus.value.forEach { (service, enabled) ->
        key(service) {
            DataSharingToolSwitch(
                modifier = modifier,
                dataSharingService = service,
                isEnabled = enabled,
                onServiceActivationStatusChange = onServiceActivationStatusChange
            )
        }
    }
}

@Composable
fun DataSharingToolSwitch(
    modifier: Modifier = Modifier,
    dataSharingService: DataSharingService,
    isEnabled: Boolean,
    onServiceActivationStatusChange: ((DataSharingService, Boolean) -> Unit)
) {
    val title = when (dataSharingService) {
        DataSharingService.GOOGLE_ANALYTICS -> stringResource(id = R.string.legal_data_sharing_google_analytics_title)
        DataSharingService.FIREBASE_CRASHLYTICS -> stringResource(id = R.string.legal_data_sharing_firebase_crashlytics_title)
    }

    val description = when (dataSharingService) {
        DataSharingService.GOOGLE_ANALYTICS -> stringResource(id = R.string.legal_data_sharing_google_analytics_description)
        DataSharingService.FIREBASE_CRASHLYTICS -> stringResource(id = R.string.legal_data_sharing_firebase_crashlytics_description)
    }

    DataSharingSwitch(
        title = title,
        description = description,
        checked = isEnabled,
        onCheckedChange = { checked ->
            onServiceActivationStatusChange.invoke(
                dataSharingService,
                checked
            )
        }
    )
}

@Composable
fun AllDataSharingToolSwitch(
    modifier: Modifier = Modifier,
    dataSharingServicesActivationStatus: State<Map<DataSharingService, Boolean>>,
    onCheckedChange: (Boolean) -> Unit
) {
    val allServicesEnabled = !dataSharingServicesActivationStatus.value.values.contains(false)
    DataSharingSwitch(
        modifier = modifier,
        title = stringResource(id = R.string.legal_data_sharing_accept_all),
        description = null,
        checked = allServicesEnabled,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun DataSharingSwitch(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()

    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }

            if (description != null) {
                Text(
                    modifier = Modifier.padding(bottom = 8.dp),
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    name = "Light Mode"
)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)

@Composable
fun PreviewDataSharingSettingsScreen() {
    DevFestNantesTheme {
        Scaffold {
            DataSharingSettingsScreen(
                modifier = Modifier.padding(it),
                dataSharingServicesActivationStatus = remember {
                    mutableStateOf(
                        mapOf(
                            DataSharingService.FIREBASE_CRASHLYTICS to true,
                            DataSharingService.GOOGLE_ANALYTICS to false
                        )
                    )
                },
                onServiceActivationStatusChange = { _, _ -> },
                onAllServicesActivationStatusChange = { },
                onBackClick = { }
            )
        }
    }
}
