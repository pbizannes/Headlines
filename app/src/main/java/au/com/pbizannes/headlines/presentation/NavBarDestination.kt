package au.com.pbizannes.headlines.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.Web
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

enum class NavBarDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String,
    val namedArguments: List<NamedNavArgument> = emptyList(),
    val isBottomNavigation: Boolean = true,
) {
    HEADLINES("headlines", "Headlines", Icons.Default.ViewHeadline, "Headlines"),
    SOURCES("sources", "Sources", Icons.Default.Source, "Sources"),
    SAVED("saved", "Saved", Icons.Default.Save, "Saved"),
    WEBVIEW(
        route = "webview/{url}",
        label = "Article",
        icon = Icons.Default.Web,
        contentDescription = "View Article",
        isBottomNavigation = false,
        namedArguments = listOf(navArgument(WebViewScreen.ARG_URL) { type = NavType.StringType })
    )
}

object WebViewScreen {
    const val ROUTE_PREFIX = "webView"
    const val ARG_URL = "url" // Argument name for the URL
    val route = "$ROUTE_PREFIX/{$ARG_URL}" // Route with a URL argument

    fun createRoute(url: String): String {
        // Important: Encode the URL to handle special characters safely
        val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
        return "$ROUTE_PREFIX/$encodedUrl"
    }
}
