package com.example.timewarp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.example.ExampleScreen
import com.example.timewarp.feature.example.RegistrationScreen
import com.example.timewarp.feature.example.StartupScreen
import com.example.timewarp.navigation.Screen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Startup.route
    ) {
        composable(Screen.Startup.route) { StartupScreen(navController = navController) }
        composable(Screen.Registration.route) { RegistrationScreen(navController = navController) }
        composable(Screen.Home.route) { ExampleScreen() }
        composable(Screen.Example.route) { ExampleScreen() }
    }
}