package au.com.pbizannes.headlines.presentation.headlines

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccompanistWebviewComponent(
    urlToLoad: String,
    onNavigateUp: () -> Unit // Callback to navigate back from this screen
) {
    val webViewState = rememberWebViewState(url = urlToLoad)
    val webViewNavigator = rememberWebViewNavigator()

    // Handle back press to navigate within WebView if possible, otherwise navigate up
    BackHandler(enabled = webViewNavigator.canGoBack) {
        webViewNavigator.navigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = webViewState.pageTitle ?: "Loading...",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (webViewNavigator.canGoBack) {
                            webViewNavigator.navigateBack()
                        } else {
                            onNavigateUp()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Optional: Add a close button to directly exit the WebView screen
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close WebView"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            val loadingState = webViewState.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = { loadingState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            WebView(
                state = webViewState,
                modifier = Modifier.weight(1f), // WebView takes remaining space
                navigator = webViewNavigator,
                onCreated = { createdWebView ->
                    // Configure WebView settings if needed
                    createdWebView.settings.javaScriptEnabled = true
                    createdWebView.settings.allowFileAccess = true
                    createdWebView.settings.setDomStorageEnabled(true);

                    // Add other settings like zoom controls, etc.
                    // webView.settings.builtInZoomControls = true
                    // webView.settings.displayZoomControls = false
                },
                // Optional: Provide a custom WebViewClient
                client = object : AccompanistWebViewClient() {
                    override fun onPageStarted(
                        view: WebView,
                        url: String?,
                        favicon: Bitmap?
                    ) {
                        super.onPageStarted(view, url, favicon)
                        // Page loading started
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        // Page loading finished
                    }
                }
            )
        }
    }
}