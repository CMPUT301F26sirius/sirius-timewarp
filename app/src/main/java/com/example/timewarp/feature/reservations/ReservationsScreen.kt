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
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.testTag

private val previewTabs = listOf(
    ReservationTab.Booked,
    ReservationTab.Booking,
    ReservationTab.Past,
    ReservationTab.Cancelled,
)

private val previewOwner = listOf(
    ReservationToDisplay.Mine,
    ReservationToDisplay.AtMyCamps,
)
/**
 * Responsible for connecting to the UI: Grabs the state, triggers loading,
 * shows loading/error/content, and handles navigation
 * @param modifier Applied to the wrapper
 * @param screen gives the title and selected bottom nav item
 * @param navController Used to open reservation or browse
 * @param viewModel Given by Hilt; holds the state and loads the data
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
    val scopes by viewModel.scopes.collectAsStateWithLifecycle()
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (val s = state) {
                is ReservationsState.Loading -> Text("Loading....")
                is ReservationsState.Error -> Text("Error: ${s.message}")
                is ReservationsState.Content -> ReservationsContent(
                    state = s,
                    tabs = tabs,
                    scopes = scopes,
                    tabSelected = viewModel::selectTab,
                    scopeSelected = viewModel::selectScope,
                    campsiteSelected = viewModel::selectCampsite,
                    reservationClick = {item ->
                        // Winners who haven't reserved yet go to make-reservation; everything else to manage.
                        val route = if (s.selectedScope == ReservationToDisplay.Mine && item.canMakeReservation) {
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
 * Responsible only for drawing the state and reporting clicks.
 *
 *
 * @param state Loaded reservations and selected tab
 * @param tabs tabs for current user
 * @param scopes Scope chips for the user (Entrants don't have them)
 * @param tabSelected the tab user clicked
 * @param scopeSelected The scope the user clicked
 * @param campsiteSelected The campsite picked in dropdown
 * @param reservationClick reservation card when the user click
 * @param skipToBrowse go to browse when there is an empty view
 * @param modifier applied to the outer column
 */
@Composable
internal fun ReservationsContent(
    state: ReservationsState.Content,
    tabs: List<ReservationTab>,
    scopes: List<ReservationToDisplay>,
    tabSelected: (ReservationTab) -> Unit,
    scopeSelected: (ReservationToDisplay) -> Unit,
    campsiteSelected: (String?) -> Unit,
    reservationClick: (ReservationItem) -> Unit,
    skipToBrowse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
       modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scope chips (owners and admins only)
        if (scopes.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scopes.forEach { scope ->
                    TabChip(
                        label = scope.label,
                        selected = scope == state.selectedScope,
                        onClick = { scopeSelected(scope) },
                        modifier = Modifier.testTag("scope_${scope.label}")
                    )
                }
            }
        }
        // Tab chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                TabChip(
                    label = tab.label,
                    selected = tab == state.selectedTab,
                    onClick = {tabSelected(tab)},
                    modifier = Modifier.testTag("tab_${tab.label}")
                )
            }
        }

        // Only the reservations for the current tab
        val items = state.visibleItems
        val isAtMyCamps = state.selectedScope == ReservationToDisplay.AtMyCamps
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (items.isEmpty()) {
                    state.selectedTab.emptyHeading
                } else {
                    state.selectedScope.heading ?: state.selectedTab.heading
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )

            if (isAtMyCamps && state.myCampsites.isNotEmpty()) {
                CampsiteDropdown(
                    campsites = state.myCampsites,
                    selectedCampsiteId = state.selectedCampsiteId,
                    onSelect = campsiteSelected,
                )
            }

        }

        if (items.isEmpty()) {
            if (!isAtMyCamps) {
                EmptyReservations(onGoToBrowse = skipToBrowse)
            }
        } else {
            items.forEach { item ->
                ReservationCard(
                    item = item,
                    tab = state.selectedTab,
                    isAtMyCamps = isAtMyCamps,
                    onClick = { reservationClick(item) },
                    modifier = Modifier.testTag("reservation_${item.reservationId}")
                )
            }
        }
    }
}

/**
 * Responsible for showing one tab's label
 *
 *
 * @param label Text on chip
 * @param selected Whether this chip is currently selected
 * @param onClick called when chip is tapped
 * @param modifier Applied to chip
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

/**
 * Responsible for letting an owner filter their camps' reservations by campsite.
 *
 * @param campsites Campsites the user owns
 * @param selectedCampsiteId The campsite currently picked, or null
 * @param onSelect Called with the picked campsite's ID, or null
 */
@Composable
private fun CampsiteDropdown(
    campsites: List<CampsiteOption>,
    selectedCampsiteId: String?,
    onSelect: (String?) -> Unit,
) {
    // Whether the list of campsites is open
    var expanded by remember { mutableStateOf(false) }

    val selectedName = campsites.firstOrNull { it.campsiteId == selectedCampsiteId }?.name ?: "All campsites"

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                .testTag("campsite_filter"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All campsites") },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            campsites.forEach { campsite ->
                DropdownMenuItem(
                    text = { Text(campsite.name) },
                    onClick = {
                        onSelect(campsite.campsiteId)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Responsible for showing one reservation
 *
 * @param item reservation to display
 * @param tab Selected tab
 * @param isAtMyCamps True when owner is looking at their owned campsites
 * @param onClick Called when the card is clicked
 * @param modifier applied to the card
 */
@Composable
private fun ReservationCard(
    item: ReservationItem,
    onClick: () -> Unit,
    tab: ReservationTab,
    isAtMyCamps: Boolean,
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
            Icon(
                imageVector = Icons.Outlined.Landscape,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center),
            )

            // Badges: the tab, plus "Upcoming" / "In progress" for active stays
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Badge(text = tab.label.uppercase())
                if (item.stayLabel.isNotBlank() && tab == ReservationTab.Booked) {
                    Badge(text = item.stayLabel.uppercase())
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (isAtMyCamps) "${item.guestName} · ${item.title}" else item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (isAtMyCamps) item.stayText.ifBlank { "No dates yet" } else tab.cardSubtitle,
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

/**
 * Responsible for one small label on top of a card's image
 *
 * @param text Label text
 */
@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 *
 * Responsible for telling the user this tab has nothing, suggest to browse
 *
 * @param onGoToBrowse Called when the view is tapped
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
            scopes = emptyList(),
            tabSelected = {},
            scopeSelected = {},
            campsiteSelected = {},
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
            scopes = emptyList(),
            tabSelected = {},
            scopeSelected = {},
            campsiteSelected = {},
            reservationClick = {},
            skipToBrowse = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReservationsAtMyCampsPreview() {
    TimewarpTheme {
        ReservationsContent(
            state = ReservationsState.Content(
                selectedTab = ReservationTab.Booked,
                selectedScope = ReservationToDisplay.AtMyCamps,
                myCampsites = listOf(CampsiteOption("c1", "Wapiti Camp")),
                items = listOf(
                    ReservationItem(
                        reservationId = "1",
                        state = ReservationState.Reserved,
                        title = "Wapiti Camp",
                        dateText = "3 days ago",
                        guestName = "John Doe",
                        stayText = "Dec 1 - Dec 5, 2026",
                        stayLabel = "Upcoming",
                        isMine = false,
                        isAtMyCamp = true,
                    ),
                ),
            ),
            tabs = previewTabs,
            scopes = previewOwner,
            tabSelected = {},
            scopeSelected = {},
            campsiteSelected = {},
            reservationClick = {},
            skipToBrowse = {},
        )
    }
}