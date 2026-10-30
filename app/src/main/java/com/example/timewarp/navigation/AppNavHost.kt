package com.example.timewarp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.core.util.deepComposable
import com.example.timewarp.feature.browse.BrowseScreen
import com.example.timewarp.feature.campsite.details.CampsiteDetailsScreen
import com.example.timewarp.feature.startup.StartupScreen
import com.example.timewarp.feature.registration.RegistrationScreen
import com.example.timewarp.navigation.Screen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Startup.route
    ) {
        // Static routes.
        composable(Screen.Startup.route) {
            StartupScreen(navController = navController)
        }
        composable(Screen.Registration.route) {
            RegistrationScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            BrowseScreen(navController = navController)
        }

        // Dynamic routes.
        deepComposable(Screen.CampsiteDetails.route) { campsiteId ->
            CampsiteDetailsScreen(
                campsiteId = campsiteId,
                navController = navController
            )
        }
    }
}