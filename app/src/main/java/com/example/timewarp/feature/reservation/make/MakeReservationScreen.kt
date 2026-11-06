package com.example.timewarp.feature.reservation.make

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.ComponentStyles
import com.example.timewarp.ui.theme.PitchError
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme

/*
* Make reservation screen for lottery winners, with the Confirm button (US 01.05.01).
* Outstanding: date picking (US 01.04.01) goes above the Confirm button.
*/

/**
 * Screen where a lottery winner confirms their reservation.
 * After confirming, opens the Reservation confirmed screen.
 *
 * @param reservationId the won reservation to confirm.
 */

@Composable
fun MakeReservationScreen(
    reservationId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.MakeReservation,
    navController: NavController = rememberNavController(),
    viewModel: MakeReservationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load the reservation when the screen opens
    LaunchedEffect(reservationId) {
        viewModel.load(reservationId)
    }

    // After confirming, open the confirmation screen
    LaunchedEffect(state) {
        val current = state
        if (current is MakeReservationState.Confirmed) {
            navController.navigate(Screen.ReservationConfirmation.createRoute(current.reservationId)) {
                // Remove this screen so Back doesn't return to an already confirmed booking
                popUpTo("${Screen.MakeReservation.route}/{id}") {
                    inclusive = true
                }
            }
        }
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        MakeReservationContent(
            state = state,
            onConfirm = { viewModel.confirm(reservationId) }
        )
    }
}

/**
 * Body of the Make reservation screen.
 *
 * @param state what to show.
 * @param onConfirm called when the user taps Confirm.
 */
@Composable
fun MakeReservationContent(
    state: MakeReservationState,
    onConfirm: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        when (state) {
            MakeReservationState.Loading -> Text("Loading reservation...")
            is MakeReservationState.Error -> Text("Error: ${state.message}", color = PitchError)
            is MakeReservationState.Confirmed -> Text("Reservation confirmed.")

            is MakeReservationState.Content -> {
                // Campsite info, or placeholders if it couldn't be loaded
                var campsiteName = "Unknown campsite"
                var address = ""
                if (state.campsite != null) {
                    campsiteName = state.campsite.name
                    address = state.campsite.location.address
                }

                Text(text = "You won!", style = MaterialTheme.typography.labelMedium)
                Text(text = campsiteName, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(text = address, color = PitchMutedText)

                DetailRow(label = "Reservation #", value = state.reservation.reservationId)
                DetailRow(label = "Status", value = state.reservation.reservationState.name)

                Spacer(modifier = Modifier.height(12.dp))

                // Only won (Pending) reservations can be confirmed
                if (state.canConfirm) {
                    Button(
                        onClick = onConfirm,
                        shape = ComponentStyles.ButtonShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.ButtonHeight)
                            .testTag("confirm_reservation_button")
                    ) {
                        Text("Confirm Reservation")
                    }
                } else {
                    Text("This reservation can't be confirmed.", color = PitchMutedText)
                }
            }
        }
    }
}

// One label/value pair, e.g. "Status" / "Pending"
@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
private fun MakeReservationContentPreview() {
    val reservation = Reservation(reservationId = "PT-271204", reservationState = ReservationState.Pending)
    val campsite = Campsite(name = "Pocahontas", location = Location(address = "Jasper, Alberta"))

    TimewarpTheme {
        MakeReservationContent(
            state = MakeReservationState.Content(reservation, campsite, canConfirm = true),
            onConfirm = {}
        )
    }
}