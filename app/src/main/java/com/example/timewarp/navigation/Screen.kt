package com.example.timewarp.navigation

sealed class Screen(val route: String) {
    data object Example : Screen("example")
}