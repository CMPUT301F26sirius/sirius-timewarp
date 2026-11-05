package com.example.timewarp.feature.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme
import com.example.timewarp.domain.model.ReservationState

private val previewTabs = listOf(
    ReservationTab.Booked,
    ReservationTab.Booking,
    ReservationTab.Past,
    ReservationTab.Cancelled,
)
/**
Main Responsibility: Responsible for connecting to the UI: Grabs the state, triggers loading,
                     shows loading/error/content, and handles navigation

Parameters: @param modifier: Applied to the wrapper
            @param screen: gives the title and selected bottom nav item
            @param navController: Used to open reservation or browse
            @param viewModel: Given by Hilt; holds the state and loads the data
 */
@Composable
fun ReservationsScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Reservations,
    navController: NavController = rememberNavController(),
    viewModel: ReservationsViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (val s = state) {
                is ReservationsState.Loading -> Text("Loading....")
                is ReservationsState.Error -> Text("Error: ${s.message}")
                is ReservationsState.Content -> ReservationsContent(
                    state = s,
                    tabs = tabs,
                    tabSelected = viewModel::selectTab,
                    reservationClick = {item ->
                        // Winners who haven't reserved yet go to make-reservation; everything else to manage.
                        val route = if (item.canMakeReservation) {
                            Screen.MakeReservation.createRoute(item.reservationId)
                        } else {
                            Screen.ReservationDetails.createRoute(item.reservationId)
                        }
                        navController.navigate(route)
                    },
                    skipToBrowse = {
                        navController.navigate(Screen.Browse.route) { launchSingleTop = true}
                    },
                )
            }
        }
    }
}

/**
Main Responsibility: Responsible only for drawing the state and reporting clicks.

Parameters: @param state: Loaded reservations and selected tab
            @param tabs: tabs for current user
            @param tabSelected: the tab user clicked
            @param reservationClick: reservation card when the user click
            @param skipToBrowse: go to browse when there is an empty view
            @param modifier: applied to the outer column
 */
@Composable
internal fun ReservationsContent(
    state: ReservationsState.Content,
    tabs: List<ReservationTab>,
    tabSelected: (ReservationTab) -> Unit,
    reservationClick: (ReservationItem) -> Unit,
    skipToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
       modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                TabChip(
                    label = tab.label,
                    selected = tab == state.selectedTab,
                    onClick = {tabSelected(tab)}
                )
            }
        }

        // Only the reservations for the current tab
        val items = state.visibleItems
        Text(
            text = if (items.isEmpty()) state.selectedTab.emptyHeading else
            state.selectedTab.heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (items.isEmpty()) {
            EmptyReservations(onGoToBrowse = skipToBrowse)
        } else {
            items.forEach { item ->
                ReservationCard(
                    item = item,
                    tab = state.selectedTab,
                    onClick = { reservationClick(item) },
                )
            }
        }
    }
}

/*
Main Responsibility: Responsible for showing one tab's label

Parameters: -> label: Text on chip
            -> selected: Whether this chip is currently selected
            -> onClick: called when chip is tapped
            -> modifier: Applied to chip
 */
@Composable
private fun TabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/*
Main Responsibility: Responsible for showing one reservation

Parameters: -> item: reservation to display
            -> onClick: Called when the card is clicked
            -> modifier: applied to the card
 */
@Composable
private fun ReservationCard(
    item: ReservationItem,
    onClick: () -> Unit,
    tab: ReservationTab,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = tab.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = tab.cardSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = item.dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
/*
Main Responsibility: Responsible for telling the user this tab has nothing, suggest to browse

Parameters: -> OnGoToBrowse: Called when the view is tapped
 */
@Composable
private fun EmptyReservations(onGoToBrowse: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 120.dp)
            .clickable(onClick = onGoToBrowse),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Explore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = "Go To Browse?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Design view preview for empty state
@Preview(showBackground = true)
@Composable
private fun ReservationsEmptyPreview() {
    TimewarpTheme {
        ReservationsContent(
            state = ReservationsState.Content(selectedTab = ReservationTab.Booking),
            tabs = previewTabs,
            tabSelected = {},
            reservationClick = {},
            skipToBrowse = {},
        )
    }
}

// Design view preview with content inside
@Preview(showBackground = true)
@Composable
private fun ReservationsScreenPreview() {
    TimewarpTheme {
        ReservationsContent(
            state = ReservationsState.Content(
                selectedTab = ReservationTab.Booking,
                items = listOf(
                    ReservationItem("1", ReservationState.Pending, "You Won Super Cool Lottery!", "Drawn 3 days ago"),
                    ReservationItem("2", ReservationState.Pending, "You Won Super Cool Lottery!", "Drawn 3 days ago"),
                ),
            ),
            tabs = previewTabs,
            tabSelected = {},
            reservationClick = {},
            skipToBrowse = {},
        )
    }
}
