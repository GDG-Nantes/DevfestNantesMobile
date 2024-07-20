package com.gdgnantes.devfest.androidapp.ui.screens.legal

import android.content.res.Configuration
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.gdgnantes.devfest.androidapp.R
import com.gdgnantes.devfest.androidapp.ui.screens.Screen
import com.gdgnantes.devfest.androidapp.ui.theme.DevFestNantesTheme

@Composable
fun LegalScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            com.gdgnantes.devfest.androidapp.ui.components.appbars.TopAppBar(
                title = stringResource(id = Screen.Legal.title),
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
        // Declare a string that contains a url
        val mUrl = "file:///android_asset/licenses.html"

        // Adding a WebView inside AndroidView
        // with layout as full screen
        AndroidView(
            modifier =
            modifier
                .padding(it)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    webViewClient = WebViewClient()
                    loadUrl(mUrl)
                }
            },
            update = { webView ->
                webView.loadUrl(mUrl)
            }
        )
    }
}

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
fun PreviewLegalScreen() {
    DevFestNantesTheme {
        LegalScreen(
            onBackClick = {}
        )
    }
}
