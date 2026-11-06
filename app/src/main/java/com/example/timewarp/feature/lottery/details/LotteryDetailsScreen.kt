package com.example.timewarp.feature.lottery.details

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import com.example.timewarp.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.IconButton
import com.example.timewarp.ui.theme.TimewarpTheme
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clip
import java.util.concurrent.TimeUnit


/**
 * Responsible for connecting to the UI: Grabs State, triggers loading, shows
 * loading/error/content, and handles navigation
 *
 * @param lotteryId ID of the lottery to show
 * @param modifier modifier Applied
 * @param screen gives the title and bottom nav
 * @param navController opens the user's reservation
 * @param viewModel Given by Hilt; holds the state, loads the lottery, joins and leaves
 */
@Composable
fun LotteryDetailsScreen(
    lotteryId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.LotteryDetails,
    navController: NavController = rememberNavController(),
    viewModel: LotteryDetailsViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(viewModel, lotteryId) {
        viewModel.observe(lotteryId)
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (val s = state) {
                is LotteryDetailsState.Loading -> Text("Loading....")
                is LotteryDetailsState.Error -> Text("Error: ${s.message}")
                is LotteryDetailsState.Content -> LotteryDetailsContent(
                    state = s,
                    joinClick = viewModel::join,
                    leaveClick = viewModel::leaveWaitlist,
                    viewReservationClick = { reservationId ->
                        navController.navigate(Screen.ReservationDetails.createRoute(reservationId))
                    },
                    editClick = {
                        // TODO: open edit lottery once that screen is made
                    },
                )
            }
        }
    }
}

/**
 * Responsible only for displaying the lottery and reporting clicks.
 *
 * @param state Loaded lottery
 * @param joinClick Join the lottery or its waitlist
 * @param leaveClick Leave the waitlist, given the user's reservation ID
 * @param viewReservationClick Open the user's reservation, given its ID
 * @param editClick Owner tapped Edit lottery
 * @param modifier applied to the outer column
 */
@Composable
internal fun LotteryDetailsContent(
    state: LotteryDetailsState.Content,
    joinClick: () -> Unit,
    leaveClick: (String) -> Unit,
    viewReservationClick: (String) -> Unit,
    editClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            // TODO: replace with the campsite photo once the team decides how images are handled.
            Icon(
                imageVector = Icons.Outlined.Landscape,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
            )
        }

        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        // Periods
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DisplayInfo("Registration", state.registrationText, Modifier.weight(1f))
            DisplayInfo("Booking", state.bookingText, Modifier.weight(1f))
        }

        // Winners and waitlist
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DisplayInfo("Winners", state.winnerCount.toString(), Modifier.weight(1f))
            DisplayInfo("Waitlist", state.waitlistText, Modifier.weight(1f))
        }

        // Campsites in this lottery
        Text(
            text = "Campsites",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (state.campsites.isEmpty()) {
            Text(
                text = "No campsites in this lottery yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.campsites.forEach { campsite ->
                CampsiteRow(campsite)
            }
        }

        // Main button
        when (val action = state.action) {
            is LotteryAction.JoinLottery -> JoinOrLeaveButton(
                text = "Join lottery",
                enabled = !state.isWorking,
                onClick = joinClick,
            )
            is LotteryAction.JoinWaitlist -> JoinOrLeaveButton(
                text = "Join waitlist",
                enabled = !state.isWorking,
                onClick = joinClick,
            )
            is LotteryAction.LeaveWaitlist -> {
                Text(
                    text = "You're on the waitlist",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                JoinOrLeaveButton(
                    text = "Leave waitlist",
                    enabled = !state.isWorking,
                    isDestructive = true,
                    onClick = { leaveClick(action.reservationId) },
                )
            }
            is LotteryAction.ViewReservation -> JoinOrLeaveButton(
                text = "View reservation",
                enabled = !state.isWorking,
                onClick = { viewReservationClick(action.reservationId) },
            )
            is LotteryAction.None -> Text(
                text = "This lottery isn't open right now",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Join or leave failed
        if (state.actionError != null) {
            Text(
                text = state.actionError,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Owners only
        if (state.isOwner) {
            OutlinedButton(
                onClick = editClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Edit lottery")
            }
        }
    }
}

/**
 * Responsible for showing one labelled piece of information.
 *
 * @param label Small text on top, e.g. "Winners"
 * @param value The information, e.g. "3"
 * @param modifier Applied to the column
 */
@Composable
private fun DisplayInfo(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/**
 * Responsible for showing one campsite in the lottery.
 *
 * @param campsite Campsite to show
 * @param modifier Applied to the row
 */
@Composable
private fun CampsiteRow(
    campsite: LotteryCampsite,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Landscape,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = campsite.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Responsible for drawing the main full-width button with a ticket icon.
 *
 * @param text Button label
 * @param enabled False while a join or leave is saving
 * @param onClick Called when tapped
 * @param isDestructive Red colors, used for Leave waitlist
 * @param modifier Applied to the button
 */
@Composable
private fun JoinOrLeaveButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        colors = if (isDestructive) {
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            )
        } else {
            ButtonDefaults.buttonColors()
        },
    ) {
        Icon(
            imageVector = Icons.Outlined.ConfirmationNumber,
            contentDescription = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

/** Preview for a user on the waitlist who owns one of the campsites. */
@Preview(showBackground = true)
@Composable
private fun LotteryDetailsPreview() {
    TimewarpTheme {
        LotteryDetailsContent(
            state = LotteryDetailsState.Content(
                title = "Wapiti Camp Lottery",
                registrationText = "Nov 1, 2026 - Nov 10, 2026",
                bookingText = "Dec 1, 2026 - Dec 31, 2026",
                winnerCount = 3,
                waitlistText = "12 / 100",
                campsites = listOf(
                    LotteryCampsite("1", "Wapiti Camp", "Jasper, AB"),
                    LotteryCampsite("2", "Whistlers Camp", "Jasper, AB"),
                ),
                action = LotteryAction.LeaveWaitlist("r1"),
                isOwner = true,
            ),
            joinClick = {},
            leaveClick = {},
            viewReservationClick = {},
            editClick = {},
        )
    }
}