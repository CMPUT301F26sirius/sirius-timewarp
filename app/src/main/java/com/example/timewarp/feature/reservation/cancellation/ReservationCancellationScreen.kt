package com.example.timewarp.feature.reservation.cancellation

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
import com.example.timewarp.ui.theme.PitchError
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.PitchMutedText

/*
* "Cancellation registered by system" screen shown after cancelling (US 01.07.01).
* Outstanding: CSV download and notification are added in US 01.07.02 (#26).
*/
@Composable
fun CancellationScreen(
    reservationId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.ReservationCancellation,
    navController: NavController = rememberNavController(),
    viewModel: CancellationViewModel = hiltViewModel()
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
                .testTag("cancellation_screen")
        ) {
            when (val current = state) {
                CancellationState.Loading -> Text("Loading...")
                is CancellationState.Error -> Text("Error: ${current.message}", color = PitchError)

                is CancellationState.Content -> {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = PitchForest,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Your cancellation has been registered by the system.",
                        color = PitchMutedText
                    )

                    // Campsite name, or a placeholder if it couldn't be loaded
                    var campsiteName = "Unknown campsite"
                    if (current.campsite != null) {
                        campsiteName = current.campsite.name
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        DetailRow(label = "Reservation #", value = current.reservation.reservationId)
                        DetailRow(label = "Campsite", value = campsiteName)
                        DetailRow(label = "Status", value = current.reservation.reservationState.name)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

// One label/value pair, e.g. "Status" / "Canceled"
@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}