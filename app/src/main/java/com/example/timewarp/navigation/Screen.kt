package com.example.timewarp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Token
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val title: String,
    val icon: ImageVector
) {
    // Static routes.
    data object Startup : Screen("startup", "Startup", "", Icons.Outlined.Home)
    data object Registration : Screen("registration", "", "Registration", Icons.Outlined.Home)
    data object Browse : Screen("browse", "Browse", "A good day starts outside.", Icons.Outlined.Search)
    data object Lotteries : Screen("lotteries", "Lotteries", "Cross your fingers!", Icons.Outlined.Token)
    data object Reservations : Screen("reservations", "Reservations", "You are on your way!", Icons.Outlined.CalendarMonth)
    data object Alerts : Screen("alerts", "Alerts", "Alerts", Icons.Outlined.Alarm)
    data object Account : Screen("account", "Account", "Account", Icons.Outlined.AccountBox)
    data object Moderate : Screen("moderate", "Moderate", "Moderate", Icons.Outlined.AddModerator)

    // Dynamic routes.
    data object CampsiteDetails : Screen("campsite/details", "Details", "Camp details", Icons.Outlined.Home) {
        fun createRoute(campsiteId: String) = "$route/$campsiteId"
    }
}