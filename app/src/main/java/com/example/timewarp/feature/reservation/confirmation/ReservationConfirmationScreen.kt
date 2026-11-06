package com.example.timewarp.feature.reservation.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.CSVButton
import com.example.timewarp.ui.theme.PitchError
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.PitchMutedText

/*
* "Reservation confirmed" screen shown after confirming a won reservation (US 01.05.01, US 01.05.02).
* Outstanding: notification isn't shown in the Alerts tab yet; no check-in/check-out dates.
*/

/**
 * Shows the confirmed reservation and a button to download it as a CSV.
 *
 * @param reservationId the reservation that was just confirmed.
 */
@Composable
fun ReservationConfirmationScreen(
    reservationId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.ReservationConfirmation,
    navController: NavController = rememberNavController(),
    viewModel: ReservationConfirmationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load the reservation when the screen opens
    LaunchedEffect(reservationId) {
        viewModel.load(reservationId)
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("confirmation_screen")
        ) {
            when (val current = state) {
                ReservationConfirmationState.Loading -> Text("Loading...")
                is ReservationConfirmationState.Error -> Text("Error: ${current.message}", color = PitchError)

                is ReservationConfirmationState.Content -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = PitchForest,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "You're all set. See you outside.",
                        color = PitchMutedText
                    )

                    // Campsite info, or placeholders if it couldn't be loaded
                    var campsiteName = "Unknown campsite"
                    var address = ""
                    if (current.campsite != null) {
                        campsiteName = current.campsite.name
                        address = current.campsite.location.address
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        DetailRow(label = "Reservation #", value = current.reservation.reservationId)
                        DetailRow(label = "Campsite", value = campsiteName)
                        DetailRow(label = "Location", value = address)
                        DetailRow(label = "Status", value = current.reservation.reservationState.name)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CSVButton(
                        csvText = createConfirmationCSV(current.reservation, current.campsite),
                        fileName = "reservation-${current.reservation.reservationId}",
                        text = "Download reservation CSV",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_csv_button")
                    )

                    BaseButton(
                        onClick = { navController.navigate(Screen.Reservations.route) },
                        text = "Back to reservations",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// One label/value pair
@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}