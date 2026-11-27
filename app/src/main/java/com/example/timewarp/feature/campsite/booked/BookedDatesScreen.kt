package com.example.timewarp.feature.campsite.booked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.usecase.BookedStay
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.PitchError
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme

/*
 * Booked dates screen: a campsite owner sees the nights already booked at their campsite (US 02.06.01).
 * Opened from the owner's "View booked dates" button on the campsite page.
 */

/**
 * Screen listing the current and upcoming booked stays at one campsite.
 *
 * @param campsiteId the campsite whose bookings are shown.
 */
@Composable
fun BookedDatesScreen(
    campsiteId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.BookedDates,
    navController: NavController = rememberNavController(),
    viewModel: BookedDatesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load the bookings when the screen opens
    LaunchedEffect(campsiteId) {
        viewModel.load(campsiteId)
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        BookedDatesContent(state = state)
    }
}

/**
 * Body of the Booked dates screen.
 *
 * @param state what to show.
 */
@Composable
fun BookedDatesContent(state: BookedDatesState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp)
    ) {
        when (state) {
            BookedDatesState.Loading -> Text("Loading booked dates...")
            is BookedDatesState.Error -> Text("Error: ${state.message}", color = PitchError)

            is BookedDatesState.Content -> {
                // Campsite name, or a placeholder if it couldn't be loaded
                var campsiteName = "Unknown campsite"
                if (state.campsite != null) {
                    campsiteName = state.campsite.name
                }

                Text(text = campsiteName, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Current and upcoming bookings", color = PitchMutedText)

                // Nothing booked yet
                if (state.stays.isEmpty()) {
                    Text(
                        text = "No nights are booked yet.",
                        color = PitchMutedText,
                        modifier = Modifier.testTag("no_booked_dates")
                    )
                }

                for (stay in state.stays) {
                    BookedStayCard(stay = stay)
                }
            }
        }
    }
}

// One booked stay: its dates, number of nights, and reservation number
@Composable
private fun BookedStayCard(stay: BookedStay) {
    // Check-out day isn't a night, so nights = check-out - check-in
    val nights = stay.checkOut - stay.checkIn
    var nightsText = "$nights nights"
    if (nights == 1) {
        nightsText = "1 night"
    }

    // Same card style as the Browse campsite cards
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("booked_stay_${stay.reservationId}")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(15.dp)
        ) {
            Text(
                text = "${DayFormatter.shortDate(stay.checkIn)} – ${DayFormatter.shortDate(stay.checkOut)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = nightsText, style = MaterialTheme.typography.bodyMedium, color = PitchMutedText)
            Text(text = "Reservation #${stay.reservationId}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_7")
@Composable
private fun BookedDatesContentPreview() {
    // Two stays around Christmas 2027
    val dec22 = 21174
    val state = BookedDatesState.Content(
        campsite = Campsite(campsiteId = "camp-1", name = "Pocahontas"),
        stays = listOf(
            BookedStay(reservationId = "PT-271204", userId = "u1", checkIn = dec22 + 2, checkOut = dec22 + 4),
            BookedStay(reservationId = "PT-271388", userId = "u2", checkIn = dec22 + 5, checkOut = dec22 + 6)
        )
    )

    TimewarpTheme {
        // Same 24dp padding and status bar space that NavigationScreen adds in the app
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            BookedDatesContent(state = state)
        }
    }
}