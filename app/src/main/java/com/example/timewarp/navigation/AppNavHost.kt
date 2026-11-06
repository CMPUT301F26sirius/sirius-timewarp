package com.example.timewarp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.core.util.deepComposable
import com.example.timewarp.feature.account.AccountScreen
import com.example.timewarp.feature.alerts.AlertsScreen
import com.example.timewarp.feature.browse.BrowseScreen
import com.example.timewarp.feature.campsite.details.CampsiteDetailsScreen
import com.example.timewarp.feature.campsite.create.CreateCampsiteScreen
import com.example.timewarp.feature.campsite.edit.EditCampsiteScreen
import com.example.timewarp.feature.lotteries.LotteriesScreen
import com.example.timewarp.feature.lottery.details.LotteryDetailsScreen
import com.example.timewarp.feature.moderate.ModerateScreen
import com.example.timewarp.feature.policy.PolicyScreen
import com.example.timewarp.feature.profile.update.UpdateProfileScreen
import com.example.timewarp.feature.qr.generate.GenerateQRScreen
import com.example.timewarp.feature.startup.StartupScreen
import com.example.timewarp.feature.registration.RegistrationScreen
import com.example.timewarp.feature.reservation.cancellation.CancellationScreen
import com.example.timewarp.feature.reservation.details.ReservationDetailsScreen
import com.example.timewarp.feature.reservations.ReservationsScreen
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
            StartupScreen(
                screen = Screen.Startup,
                navController = navController
            )
        }
        composable(Screen.Registration.route) {
            RegistrationScreen(
                screen = Screen.Registration,
                navController = navController
            )
        }
        composable(Screen.Browse.route) {
            BrowseScreen(
                screen = Screen.Browse,
                navController = navController
            )
        }
        composable(Screen.Lotteries.route) {
            LotteriesScreen(
                screen = Screen.Lotteries,
                navController = navController
            )
        }
        composable(Screen.Reservations.route) {
            ReservationsScreen(
                screen = Screen.Reservations,
                navController = navController
            )
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(
                screen = Screen.Alerts,
                navController = navController
            )
        }
        composable(Screen.Account.route) {
            AccountScreen(
                screen = Screen.Account,
                navController = navController
            )
        }
        composable(Screen.Moderate.route) {
            ModerateScreen(
                screen = Screen.Moderate,
                navController = navController
            )
        }
        composable(Screen.UpdateProfile.route) {
            UpdateProfileScreen(
                screen = Screen.UpdateProfile,
                navController = navController
            )
        }
        composable(Screen.Policy.route) {
            PolicyScreen(
                screen = Screen.Policy,
                navController = navController
            )
        }
        composable(Screen.CreateCampsite.route) {
            CreateCampsiteScreen(
                screen = Screen.CreateCampsite,
                navController = navController
            )
        }



        // Dynamic routes.
        deepComposable(Screen.LotteryDetails.route) { lotteryId ->
            LotteryDetailsScreen(
                screen = Screen.LotteryDetails,
                lotteryId = lotteryId,
                navController = navController
            )
        }
        deepComposable(Screen.CampsiteDetails.route) { campsiteId ->
            CampsiteDetailsScreen(
                screen = Screen.CampsiteDetails,
                campsiteId = campsiteId,
                navController = navController
            )
        }
        deepComposable(Screen.EditCampsite.route) { campsiteId ->
            EditCampsiteScreen(
                screen = Screen.CampsiteDetails,
                campsiteId = campsiteId,
                navController = navController
            )
        }
        deepComposable(Screen.ReservationDetails.route) { reservationId ->
            ReservationDetailsScreen(
                screen = Screen.ReservationDetails,
                reservationId = reservationId,
                navController = navController
            )
        }
        deepComposable(Screen.ReservationCancellation.route) { reservationId ->
            CancellationScreen(
                screen = Screen.ReservationCancellation,
                reservationId = reservationId,
                navController = navController
            )
        }
        deepComposable(Screen.GenerateQR.route) { url ->
            GenerateQRScreen(
                screen = Screen.GenerateQR,
                url = url,
                navController = navController
            )
        }
    }
}