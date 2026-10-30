package com.example.timewarp.core.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

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