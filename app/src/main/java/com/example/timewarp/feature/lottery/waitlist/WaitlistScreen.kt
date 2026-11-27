package com.example.timewarp.feature.lottery.waitlist

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
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.TimewarpTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase

/**
 * [WaitlistScreen]
 */
@Composable
fun WaitlistScreen(
    lotteryId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.ViewWaitlist,
    navController: NavController = rememberNavController(),
    viewModel: WaitlistViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(lotteryId) {
        viewModel.observe(lotteryId)
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        when (val s = state) {
            is WaitlistState.Loading -> Text("Loading....")
            is WaitlistState.Error -> Text("Error: ${s.message}")
            is WaitlistState.Content -> WaitListContent(
                lotteryId = s.lotteryId,
                reservations = s.reservations,
                userNames = s.userNames,
            )
        }
    }
}

/**
 * Display the content for Waitlist Screen
 *
 * @param lotteryId ID of the lottery to show waitlist for
 * @param reservations filtered list of reservations to display
 */
@Composable
fun WaitListContent(
    lotteryId: String,
    reservations: List<Reservation>,
    userNames: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Lottery  [$lotteryId] Waitlist",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        WaitlistList(
            reservations = reservations,
            modifier = Modifier,
            userNames = userNames,
        )
    }
}

/**
 * Shows list of all waitlisted reservations
 *
 * @param reservations the list of filtered reservations
 */
@Composable
private fun WaitlistList(
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
    userNames: Map<String, String>,
) {
    if (reservations.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Empty Waitlist",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = reservations,
            ) { reservation ->
                WaitlistEntry(
                    reservation = reservation,
                    userName = userNames[reservation.userId] ?: reservation.userId,
                )
            }
        }
    }
}

/**
 * Card for a single entry of a waitlist reservation
 *
 * @param reservation the reservation to display
 */
@Composable
private fun WaitlistEntry(
    reservation: Reservation,
    userName: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),

        ) {

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Reservee: $userName",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "User ID: ${reservation.userId}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Reservation ID: ${reservation.reservationId}",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun EditLotteryPreview() {
    TimewarpTheme() {
        WaitlistScreen(
            lotteryId = "0",
            viewModel = hiltViewModel()
        )
    }
}