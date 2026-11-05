package com.example.timewarp.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.AddModerator
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.QrCode
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
    data object Account : Screen("account", "Account", "Mange Account", Icons.Outlined.AccountBox)
    data object Moderate : Screen("moderate", "Moderate", "Moderate", Icons.Outlined.AddModerator)
    data object UpdateProfile : Screen("profile/update", "Update Profile", "Update Profile",  Icons.Outlined.AccountBox)
    data object Policy : Screen("policy", "Policy", "App Policy & Guidelines",  Icons.Outlined.Policy)

    data object CreateLottery : Screen("createLottery", "CreateLottery", "Create Lottery", Icons.Outlined.Token)

    // Dynamic routes.
    data object CampsiteDetails : Screen("campsite/details", "Details", "Camp details", Icons.Outlined.Home) {
        fun createRoute(campsiteId: String) = "$route/$campsiteId"
    }
    data object ReservationDetails : Screen("reservation/details", "Details", "Manage reservation", Icons.Outlined.CalendarMonth) {
        fun createRoute(reservationId: String) = "$route/$reservationId"
    }
    data object ReservationCancellation : Screen("reservation/cancellation", "Cancellation", "Cancellation registered", Icons.Outlined.CalendarMonth) {
        fun createRoute(reservationId: String) = "$route/$reservationId"
    }
    data object MakeReservation : Screen("reservation/make", "Reserve", "Make a reservation", Icons.Outlined.CalendarMonth) {
        fun createRoute(reservationId: String) = "$route/$reservationId"
    }

    data object GenerateQR : Screen("qr/generate", "QR Generation", "Share your site!", Icons.Outlined.QrCode) {
        fun createRoute(url: String) = "$route/${Uri.encode(url)}"
    }
}