package com.example.timewarp.feature.reservations

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun ReservationsScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Reservations,
    navController: NavController = rememberNavController(),
    viewModel: ReservationsViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(state) {

    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        // TODO: temporary until the reservations list (US 01.06.01) exists
        BaseButton(
            onClick = { navController.navigate(Screen.ReservationDetails.createRoute("test-reservation")) },
            text = "Open test reservation"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationsScreenPreview() {
    TimewarpTheme() {
        ReservationsScreen()
    }
}