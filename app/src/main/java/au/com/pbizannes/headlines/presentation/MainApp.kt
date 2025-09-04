package au.com.pbizannes.headlines.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import au.com.pbizannes.headlines.presentation.headlines.AccompanistWebviewComponent
import au.com.pbizannes.headlines.presentation.headlines.HeadlinesScreen
import au.com.pbizannes.headlines.presentation.saved.SavedScreen
import au.com.pbizannes.headlines.presentation.sources.SourcesScreen

@Preview()
@Composable
fun MainApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val startDestination = NavBarDestination.HEADLINES
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                NavBarDestination.entries.filter { it.isBottomNavigation }.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestination == index,
                        onClick = {
                            navController.navigate(route = destination.route)
                            selectedDestination = index
                        },
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.contentDescription
                            )
                        },
                        label = { Text(destination.label) },
                        modifier = Modifier.testTag(destination.label)
                    )
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(navController, startDestination, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: NavBarDestination,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController,
        startDestination = startDestination.route
    ) {
        NavBarDestination.entries.forEach { destination ->
            composable(
                destination.route,
                arguments = destination.namedArguments
            ) { backStackEntry ->
                when (destination) {
                    NavBarDestination.HEADLINES -> HeadlinesScreen(
                        { article ->
                            article.url.let { url ->
                                navController.navigate(WebViewScreen.createRoute(url))
                            }
                        }
                    )

                    NavBarDestination.SOURCES -> SourcesScreen()
                    NavBarDestination.SAVED -> SavedScreen(navController = navController)
                    NavBarDestination.WEBVIEW -> {
                        val encodedUrl = backStackEntry.arguments?.getString(WebViewScreen.ARG_URL)
                        val url = encodedUrl?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                        if (url != null) {
                            AccompanistWebviewComponent(
                                urlToLoad = url,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        } else {
                            // Handle error: URL not provided
                            navController.navigateUp()
                        }
                    }
                }
            }
        }
    }
}
