package com.example.timewarp.feature.reservation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
* Manage reservation screen with the Cancel Booking button (US 01.07.01).
* Outstanding: dates not shown yet; editing a reservation isn't supported.
*/

@Composable
fun ReservationDetailsScreen(
    reservationId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.ReservationDetails,
    navController: NavController = rememberNavController(),
    viewModel: ReservationDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load the reservation when the screen opens
    LaunchedEffect(reservationId) {
        viewModel.load(reservationId)
    }

    // After cancelling, open the cancellation screen
    LaunchedEffect(state) {
        val current = state
        if (current is ReservationDetailsState.Cancelled) {
            navController.navigate(Screen.ReservationCancellation.createRoute(current.reservationId)) {
                // Remove this screen so Back doesn't return to a cancelled booking
                popUpTo("${Screen.ReservationDetails.route}/{id}") {
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
        ReservationDetailsContent(
            state = state,
            onCancelConfirmed = { viewModel.cancel(reservationId) }
        )
    }
}

/**
 * Body of the Manage reservation screen.
 * Kept separate from the ViewModel so UI tests can use it without Firebase.
 *
 * @param state what to show.
 * @param onCancelConfirmed called after the user confirms the cancel dialog.
 */
@Composable
fun ReservationDetailsContent(
    state: ReservationDetailsState,
    onCancelConfirmed: () -> Unit
) {
    // Whether the "Cancel booking?" dialog is open
    var showDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        when (state) {
            ReservationDetailsState.Loading -> Text("Loading reservation...")
            is ReservationDetailsState.Error -> Text("Error: ${state.message}", color = PitchError)
            is ReservationDetailsState.Cancelled -> Text("Reservation cancelled.")

            is ReservationDetailsState.Content -> {
                // Campsite info, or placeholders if it couldn't be loaded
                var campsiteName = "Unknown campsite"
                var address = ""
                if (state.campsite != null) {
                    campsiteName = state.campsite.name
                    address = state.campsite.location.address
                }

                Text(text = campsiteName, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(text = address, color = PitchMutedText)

                DetailRow(label = "Reservation #", value = state.reservation.reservationId)
                DetailRow(label = "Status", value = state.reservation.reservationState.name)
                DetailRow(label = "Check-in / Check-out", value = "Not available yet")

                Spacer(modifier = Modifier.height(12.dp))

                // Only booked reservations show the cancel button
                if (state.canCancel) {
                    Button(
                        onClick = { showDialog = true },
                        shape = ComponentStyles.ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PitchError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.ButtonHeight)
                            .testTag("cancel_booking_button")
                    ) {
                        Text("Cancel Booking")
                    }
                } else {
                    Text("This reservation can't be cancelled.", color = PitchMutedText)
                }
            }
        }
    }

    // Confirmation dialog before cancelling
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cancel booking?") },
            text = { Text("Your spot will be released. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onCancelConfirmed()
                    },
                    modifier = Modifier.testTag("confirm_cancel_button")
                ) {
                    Text("Yes, cancel", color = PitchError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Keep booking")
                }
            }
        )
    }
}

// One label/value pair, e.g. "Status" / "Reserved"
@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservationDetailsContentPreview() {
    val reservation = Reservation(reservationId = "PT-271204", reservationState = ReservationState.Reserved)
    val campsite = Campsite(name = "Wapiti Camp", location = Location(address = "Jasper, Alberta"))

    TimewarpTheme {
        ReservationDetailsContent(
            state = ReservationDetailsState.Content(reservation, campsite, canCancel = true),
            onCancelConfirmed = {}
        )
    }
}