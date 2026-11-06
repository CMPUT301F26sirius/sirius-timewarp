package com.example.timewarp.core.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

/**
 * Extension for NavGraphBuilder that simplifies deeplinking schema
 * at the cost of being a bit rigid. Can be refactored nicer later.
 *
 * @param route - base route
 * @param scheme - e.g. <scheme>://route/<key>
 * @param content - Composable content to display for this route
 */
fun NavGraphBuilder.deepComposable(
    route: String,
    scheme: String = "pitch",
    content: @Composable (id: String) -> Unit
) {
    val key = "id"
    val fullRoute = "$route/{$key}"
    val deepLinkUriPattern = "$scheme://$route/{$key}"
    composable(
        route = fullRoute,
        arguments = listOf(
            navArgument(key) { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = deepLinkUriPattern }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getString(key).orEmpty()
        content(id)
    }
}