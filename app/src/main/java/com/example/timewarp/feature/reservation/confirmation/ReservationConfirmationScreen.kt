package com.example.timewarp.feature.reservation.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.domain.model.Eric_Reservation
import com.example.timewarp.ui.theme.PitchCream
import com.example.timewarp.ui.theme.TimewarpTheme
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ReservationConfirmationScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ReservationConfirmationViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // User Interface
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val current = state) {
                // Loading Spinner
                ReservationConfirmationState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        color = PitchCream,
                        trackColor = Color.Transparent,
                        strokeWidth = 4.dp,
                    )
                }

                // Reservation Confirmation Success
                is ReservationConfirmationState.Success -> {
                    Column(
                        verticalArrangement =  Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "RESERVATION CONFIRMED",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 36.sp * 1.06
                            )
                            Text(
                                text = "You're all set.\nSee you outside.",
                                fontSize = 10.sp,
                            )
                        }
                        ReservationSummary(current.reservation)
                    }
                    ReservationCSVButton(current.reservation)
                }

                // TODO: Empty State
                ReservationConfirmationState.Empty -> Unit

                //TODO: Error State
                is ReservationConfirmationState.Error -> Unit
            }
        }
    }
}

@Composable
private fun ReservationSummary(
    reservation: Eric_Reservation,
    modifier: Modifier = Modifier,
) {
    val details = listOf(
        "Reservation #" to reservation.reservationId,
        "Booked on" to reservation.timestamp.toString(),
        "Check-in" to reservation.startDate,
        "Check-out" to reservation.endDate,
        "Booked by" to reservation.entrant.name,
        "Campsite" to reservation.campsite.name,
        "Location" to reservation.campsite.location.address,
    )
    Card( modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            details.forEach { (label, value) ->
                Column {
                    Text (
                        text = "${label}: ",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text (
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationConfirmationScreenPreview() {
    TimewarpTheme() {
        ReservationConfirmationScreen()
    }
}
