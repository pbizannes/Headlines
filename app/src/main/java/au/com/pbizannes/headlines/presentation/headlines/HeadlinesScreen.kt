package au.com.pbizannes.headlines.presentation.headlines

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import au.com.pbizannes.headlines.R
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.presentation.HeadlinesUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadlinesScreen(
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HeadlinesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.headlinesUIState.collectAsState()

    LaunchedEffect(true) {
        viewModel.loadContent()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.headlines_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                modifier = Modifier.testTag(stringResource(R.string.headlines_title))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HeadlinesUIState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HeadlinesUIState.Success -> {
                    if (state.articles.isEmpty()) {
                        Text(
                            text = "No headlines found.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        HeadlinesList(
                            articles = state.articles,
                            onArticleClick = onArticleClick
                        )
                    }
                }
                is HeadlinesUIState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

}
