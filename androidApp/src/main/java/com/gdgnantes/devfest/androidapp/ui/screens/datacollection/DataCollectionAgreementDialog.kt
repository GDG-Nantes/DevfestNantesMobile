package com.gdgnantes.devfest.androidapp.ui.screens.datacollection

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.gdgnantes.devfest.androidapp.R

@Composable
fun DataCollectionAgreementDialog(
    modifier: Modifier = Modifier,
    viewModel: DataCollectionViewModel = hiltViewModel(),
    onOpenDataSharing: () -> Unit,
) {
    val openDialog =
        remember { mutableStateOf(!viewModel.isDataCollectionAgreementSet) }
    if (openDialog.value) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = stringResource(id = R.string.screen_data_collection))
            },
            text = {
                Text(text = stringResource(id = R.string.legal_data_collection_consent_dialog_body))
            },
            confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.onAllServicesActivationStatusChange(true)
                        openDialog.value = false
                    }
                ) {
                    Text(stringResource(id = R.string.legal_data_collection_consent_dialog_button_consent))
                }
            },
            dismissButton = {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        openDialog.value = false
                        onOpenDataSharing()
                    }
                ) {
                    Text(stringResource(id = R.string.button_dialog_data_collection_consent_customize))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }
}