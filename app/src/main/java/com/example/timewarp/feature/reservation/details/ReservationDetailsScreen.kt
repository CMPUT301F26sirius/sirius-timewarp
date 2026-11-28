package com.example.timewarp.feature.reservation.details

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
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
import com.example.timewarp.core.util.DayFormatter
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
* Manage reservation screen. The buttons depend on who is viewing:
* the entrant can cancel a booking (US 01.07.01), or book or decline a win (US 01.04.03);
* the campsite owner or an admin can reject it (US 02.08.01).
* Outstanding: editing a reservation isn't supported.
*/

/**
 * Screen showing one reservation with the actions the current user can take on it.
 *
 * @param reservationId the reservation to show.
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

    // React to finished actions
    LaunchedEffect(state) {
        val current = state

        // After cancelling, open the cancellation screen
        if (current is ReservationDetailsState.Cancelled) {
            navController.navigate(Screen.ReservationCancellation.createRoute(current.reservationId)) {
                // Remove this screen so Back doesn't return to a cancelled booking
                popUpTo("${Screen.ReservationDetails.route}/{id}") {
                    inclusive = true
                }
            }
        }

        // After declining or rejecting, go back to the previous screen (US 01.04.03)
        if (current is ReservationDetailsState.Declined || current is ReservationDetailsState.Rejected) {
            navController.popBackStack()
        }
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        ReservationDetailsContent(
            state = state,
            onCancelConfirmed = { viewModel.cancel(reservationId) },
            onMakeReservation = { navController.navigate(Screen.MakeReservation.createRoute(reservationId)) },
            onDeclineConfirmed = { viewModel.decline(reservationId) },
            onRejectConfirmed = { viewModel.reject(reservationId) }
        )
    }
}

/**
 * Body of the Manage reservation screen.
 * Kept separate from the ViewModel so UI tests can use it without Firebase.
 *
 * @param state what to show.
 * @param onCancelConfirmed called after the entrant confirms the cancel dialog.
 * @param onMakeReservation called when the entrant taps Make reservation.
 * @param onDeclineConfirmed called after the entrant confirms the decline dialog.
 * @param onRejectConfirmed called after the owner or admin confirms the reject dialog.
 */
@Composable
fun ReservationDetailsContent(
    state: ReservationDetailsState,
    onCancelConfirmed: () -> Unit,
    onMakeReservation: () -> Unit,
    onDeclineConfirmed: () -> Unit,
    onRejectConfirmed: () -> Unit
) {
    // Which confirmation dialog is open
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDeclineDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

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
            ReservationDetailsState.Declined -> Text("Reservation declined.")
            ReservationDetailsState.Rejected -> Text("Reservation rejected.")

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

                // Booked dates, if this reservation has them (US 02.06.01)
                var datesText = "Not booked yet"
                val checkIn = DayFormatter.fromDateString(state.reservation.datesBooked.startDate)
                val checkOut = DayFormatter.fromDateString(state.reservation.datesBooked.endDate)
                if (checkIn != null && checkOut != null) {
                    datesText = "${DayFormatter.shortDate(checkIn)} – ${DayFormatter.shortDate(checkOut)}"
                }
                DetailRow(label = "Check-in / Check-out", value = datesText)

                Spacer(modifier = Modifier.height(12.dp))

                // Entrant's won reservation: book it on the Make reservation screen
                if (state.canMakeReservation) {
                    Button(
                        onClick = onMakeReservation,
                        shape = ComponentStyles.ButtonShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.ButtonHeight)
                            .testTag("make_reservation_button")
                    ) {
                        Text("Make reservation")
                    }
                }

                // Entrant's won reservation: turn it down (US 01.04.03)
                if (state.canDecline) {
                    OutlinedButton(
                        onClick = { showDeclineDialog = true },
                        shape = ComponentStyles.ButtonShape,
                        border = BorderStroke(1.dp, PitchError),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PitchError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.ButtonHeight)
                            .testTag("decline_reservation_button")
                    ) {
                        Text("Decline reservation")
                    }
                }

                // Entrant's booked stay: cancel it (US 01.07.01)
                if (state.canCancel) {
                    Button(
                        onClick = { showCancelDialog = true },
                        shape = ComponentStyles.ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PitchError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.ButtonHeight)
                            .testTag("cancel_booking_button")
                    ) {
                        Text("Cancel Booking")
                    }
                }

                // Campsite owner or admin: reject it (US 02.08.01)
                if (state.canReject) {
                    Button(
                        onClick = { showRejectDialog = true },
                        shape = ComponentStyles.ButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PitchError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.ButtonHeight)
                            .testTag("reject_reservation_button")
                    ) {
                        Text("Reject reservation")
                    }
                }

                // Nothing this user can do here
                if (!state.canMakeReservation && !state.canDecline && !state.canCancel && !state.canReject) {
                    Text("This reservation can't be changed.", color = PitchMutedText)
                }
            }
        }
    }

    // Confirmation before cancelling a booked stay
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel booking?") },
            text = { Text("Your spot will be released. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        onCancelConfirmed()
                    },
                    modifier = Modifier.testTag("confirm_cancel_button")
                ) {
                    Text("Yes, cancel", color = PitchError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep booking")
                }
            }
        )
    }

    // Confirmation before declining a win
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("Decline reservation?") },
            text = { Text("Your spot will go to someone else from the lottery. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeclineDialog = false
                        onDeclineConfirmed()
                    },
                    modifier = Modifier.testTag("confirm_decline_button")
                ) {
                    Text("Yes, decline", color = PitchError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text("Keep it")
                }
            }
        )
    }

    // Confirmation before rejecting someone's reservation
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject reservation?") },
            text = { Text("The entrant will be notified and their spot goes back to the lottery. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRejectDialog = false
                        onRejectConfirmed()
                    },
                    modifier = Modifier.testTag("confirm_reject_button")
                ) {
                    Text("Yes, reject", color = PitchError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Go back")
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
    // A won reservation seen by its entrant: Make reservation and Decline
    val reservation = Reservation(reservationId = "PT-271204", reservationState = ReservationState.Pending)
    val campsite = Campsite(name = "Wapiti Camp", location = Location(address = "Jasper, Alberta"))

    TimewarpTheme {
        ReservationDetailsContent(
            state = ReservationDetailsState.Content(
                reservation = reservation,
                campsite = campsite,
                canCancel = false,
                canMakeReservation = true,
                canDecline = true
            ),
            onCancelConfirmed = {},
            onMakeReservation = {},
            onDeclineConfirmed = {},
            onRejectConfirmed = {}
        )
    }
}