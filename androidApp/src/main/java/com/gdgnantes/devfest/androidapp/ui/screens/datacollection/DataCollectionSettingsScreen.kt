package com.gdgnantes.devfest.androidapp.ui.screens.datacollection

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.services.DataCollectionService
import com.gdgnantes.devfest.androidapp.ui.screens.Screen
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme

@Composable
fun DataCollectionSettingsScreen(
    viewModel: DataCollectionViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    DataCollectionSettingsScreen(
        dataCollectionServicesActivationStatus =
        viewModel.dataCollectionServicesActivationStatus.collectAsState(
            initial = emptyMap()
        ),
        onServiceActivationStatusChange = viewModel::onServiceActivationStatusChange,
        onAllServicesActivationStatusChange = viewModel::onAllServicesActivationStatusChange,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCollectionSettingsScreen(
    modifier: Modifier = Modifier,
    dataCollectionServicesActivationStatus: State<Map<DataCollectionService, Boolean>>,
    onServiceActivationStatusChange: ((DataCollectionService, Boolean) -> Unit),
    onAllServicesActivationStatusChange: ((Boolean) -> Unit),
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar(
                title = stringResource(id = Screen.DataCollection.title),
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
            modifier =
            modifier
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
                    text =
                    stringResource(
                        id = R.string.legal_data_collection_body
                    ),
                    modifier =
                    Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )

                AllDataCollectionToolSwitch(
                    modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    dataCollectionServicesActivationStatus,
                ) { checked ->
                    onAllServicesActivationStatusChange(checked)
                }

                DataCollectionServices(
                    dataCollectionServicesActivationStatus = dataCollectionServicesActivationStatus,
                    onServiceActivationStatusChange = onServiceActivationStatusChange
                )
            }
        }
    }
}

@Composable
fun DataCollectionServices(
    modifier: Modifier = Modifier,
    dataCollectionServicesActivationStatus: State<Map<DataCollectionService, Boolean>>,
    onServiceActivationStatusChange: ((DataCollectionService, Boolean) -> Unit),
) {
    dataCollectionServicesActivationStatus.value.forEach { (service, enabled) ->
        key(service) {
            DataCollectionToolSwitch(
                modifier = modifier,
                dataCollectionService = service,
                isEnabled = enabled,
                onServiceActivationStatusChange = onServiceActivationStatusChange
            )
        }
    }
}

@Composable
fun DataCollectionToolSwitch(
    modifier: Modifier = Modifier,
    dataCollectionService: DataCollectionService,
    isEnabled: Boolean,
    onServiceActivationStatusChange: ((DataCollectionService, Boolean) -> Unit)
) {
    val title =
        when (dataCollectionService) {
            DataCollectionService.GOOGLE_ANALYTICS ->
                stringResource(
                    id = R.string.legal_data_collection_google_analytics_title
                )

            DataCollectionService.FIREBASE_CRASHLYTICS ->
                stringResource(
                    id = R.string.legal_data_collection_firebase_crashlytics_title
                )

            DataCollectionService.FIREBASE_PERFORMANCE ->
                stringResource(
                    id = R.string.legal_data_collection_firebase_performance_title
                )
        }

    val description =
        when (dataCollectionService) {
            DataCollectionService.GOOGLE_ANALYTICS ->
                stringResource(
                    id = R.string.legal_data_collection_google_analytics_description
                )

            DataCollectionService.FIREBASE_CRASHLYTICS ->
                stringResource(
                    id = R.string.legal_data_collection_firebase_crashlytics_description
                )

            DataCollectionService.FIREBASE_PERFORMANCE ->
                stringResource(
                    id = R.string.legal_data_collection_firebase_performance_description
                )
        }

    DataCollectionSwitch(
        modifier = modifier,
        title = title,
        description = description,
        checked = isEnabled,
        onCheckedChange = { checked ->
            onServiceActivationStatusChange.invoke(
                dataCollectionService,
                checked
            )
        }
    )
}

@Composable
fun AllDataCollectionToolSwitch(
    modifier: Modifier = Modifier,
    dataCollectionServicesActivationStatus: State<Map<DataCollectionService, Boolean>>,
    onCheckedChange: (Boolean) -> Unit
) {
    val allServicesEnabled = !dataCollectionServicesActivationStatus.value.values.contains(false)
    DataCollectionSwitch(
        modifier = modifier,
        title = stringResource(id = R.string.legal_data_collection_accept_all),
        description = null,
        checked = allServicesEnabled,
        onCheckedChange = onCheckedChange
    )
}

@Composable
fun DataCollectionSwitch(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        modifier =
        modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier =
            Modifier
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
fun PreviewDataCollectionSettingsScreen() {
    DevFestNantesTheme {
        Scaffold {
            DataCollectionSettingsScreen(
                modifier = Modifier.padding(it),
                dataCollectionServicesActivationStatus =
                remember {
                    mutableStateOf(
                        mapOf(
                            DataCollectionService.FIREBASE_CRASHLYTICS to true,
                            DataCollectionService.GOOGLE_ANALYTICS to false,
                            DataCollectionService.FIREBASE_PERFORMANCE to false
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
