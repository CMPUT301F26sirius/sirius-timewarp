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
import com.example.timewarp.feature.lottery.waitlist.WaitlistViewModel
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.TimewarpTheme

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.IconButton
import com.example.timewarp.ui.theme.TimewarpTheme
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.clip
import java.util.concurrent.TimeUnit

/**
 * [WaitlistScreen]
 */
@Composable
fun WaitlistScreen(
    lotteryId: String,
    modifier: Modifier = Modifier,
    // screen: Screen = Screen.Waitlist,
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
        // screen = screen,
        modifier = modifier,
        navController = navController,
    ) {

    }
}

@Composable
fun WaitListContent(
    lotteryID: String,
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Lottery #$lotteryID Waitlist",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        WaitlistList(
            reservations = reservations,
            modifier = Modifier,
        )
    }
}

@Composable
private fun WaitlistList(
    reservations: List<Reservation>,
    modifier: Modifier = Modifier,
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
                WaitlistEntry(reservation)
            }
        }
    }
}

// TODO: STYLE THIS BETTER
//  PROBABLY FETCH USER'S NAME
@Composable
private fun WaitlistEntry(
    reservation: Reservation,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),

        ) {
            Text(
                text = "User ID: ${reservation.userId}",
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Reservation ID: ${reservation.reservationId}",
                style = MaterialTheme.typography.labelMedium,
            )
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