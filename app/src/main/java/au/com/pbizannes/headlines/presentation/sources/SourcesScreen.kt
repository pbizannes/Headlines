package au.com.pbizannes.headlines.presentation.sources

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import au.com.pbizannes.headlines.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    viewModel: SourcesViewModel = hiltViewModel()
    // You can add NavController here if needed for navigation from this screen
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.sources_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Or another suitable color
                    titleContentColor = Color.White
                ),
                actions = {
                    // Optional: Add Select All / Deselect All buttons
                    val currentState = uiState
                    if (currentState is SourcesScreenUiState.Success && currentState.sourceItems.isNotEmpty()) {
                        Row {
                            TextButton(onClick = { viewModel.selectAllSources() }) {
                                Text("ALL", color = Color.White)
                            }
                            TextButton(onClick = { viewModel.deselectAllSources() }) {
                                Text("NONE", color = Color.White)
                            }
                        }
                    }
                },
                modifier = Modifier.testTag(stringResource(R.string.sources_title))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SourcesScreenUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SourcesScreenUiState.Success -> {
                    if (state.sourceItems.isEmpty()) {
                        Text(
                            text = "No news sources available.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    } else {
                        SourcesList(
                            sourceItems = state.sourceItems,
                            onCheckedChanged = { sourceId, isSelected ->
                                viewModel.onSourceSelectedChange(sourceId, isSelected)
                            }
                        )
                    }
                }
                is SourcesScreenUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { /* Consider adding a retry mechanism */ }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SourcesList(
    sourceItems: List<SourceItemUiState>,
    onCheckedChanged: (sourceId: String, isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = sourceItems,
            key = { itemState -> itemState.source.id ?: itemState.source.name } // Use id or name as key
        ) { itemState ->
            SourceListItem(
                itemState = itemState,
                onCheckedChange = onCheckedChanged
            )
        }
    }
}
