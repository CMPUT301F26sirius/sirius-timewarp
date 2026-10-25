package com.example.timewarp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.example.ExampleScreen
import com.example.timewarp.navigation.Screen

@Composable
fun TimewarpNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Example.route
    ) {
        composable(Screen.Example.route) { ExampleScreen() }
    }
}