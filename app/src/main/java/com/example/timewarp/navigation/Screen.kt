package com.example.timewarp.navigation

sealed class Screen(val route: String) {
    data object Startup : Screen("startup")
    data object Registration : Screen("registration")
    data object Example : Screen("example")
    data object Home : Screen("home")
}