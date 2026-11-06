package com.example.timewarp.feature.lotteries

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
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme
import kotlin.collections.forEach

private val previewTabs = listOf(LotteryTab.Entered, LotteryTab.Waitlisted)

/**
 * Responsible for connecting to the UI: Grabs State, triggers loading, shows
 * loading/error/content, and handles navigation
 *
 * @param modifier Applied to the wrapper
 * @param screen gives the title and selected bottom nav item
 * @param navController Used to open lottery or browse
 * @param viewModel Given by Hilt; holds the state and loads the data
 */
@Composable
fun LotteriesScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Lotteries,
    navController: NavController = rememberNavController(),
    viewModel: LotteriesViewModel = hiltViewModel(),
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (val s = state) {
                is LotteriesState.Loading -> Text("Loading....")
                is LotteriesState.Error -> Text("Error: ${s.message}")
                is LotteriesState.Content -> LotteriesContent(
                    state = s,
                    tabs = tabs,
                    tabSelected = viewModel::selectTab,
                    lotteryClick = {item ->
                        navController.navigate(Screen.LotteryDetails.createRoute(item.lotteryId))
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
 * @param state Loaded lotteries and selected tab
 * @param tabs tabs for current user
 * @param tabSelected the tab user clicked
 * @param lotteryClick lottery card when the user click
 * @param skipToBrowse go to browse when there is an empty view
 * @param modifier applied to the outer column
 */
@Composable
internal fun LotteriesContent(
    state: LotteriesState.Content,
    tabs: List<LotteryTab>,
    tabSelected: (LotteryTab) -> Unit,
    lotteryClick: (LotteryItem) -> Unit,
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

        // Only the lotteries for the current tab
        val items = state.visibleItems
        Text(
            text = if (items.isEmpty()) state.selectedTab.emptyHeading else
                state.selectedTab.heading,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (items.isEmpty()) {
            EmptyLotteries(onGoToBrowse = skipToBrowse)
        } else {
            items.forEach { item ->
                LotteryCard(
                    item = item,
                    tab = state.selectedTab,
                    onClick = { lotteryClick(item) },
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
 * Responsible for showing one lottery
 *
 * @param item lottery to display
 * @param onClick Called when the card is clicked
 * @param tab Selected tab
 * @param modifier applied to the card
 */
@Composable
private fun LotteryCard(
    item: LotteryItem,
    onClick: () -> Unit,
    tab: LotteryTab,
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
                    text = item.subtitle,
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
 *
 * Responsible for telling the user this tab has nothing, suggest to browse
 *
 * @param onGoToBrowse Called when the view is tapped
 */
@Composable
private fun EmptyLotteries(onGoToBrowse: () -> Unit) {
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

/** Preview for the empty state, on the default (Entered) tab. */
@Preview(showBackground = true)
@Composable
private fun LotteriesEmptyPreview() {
    TimewarpTheme {
        LotteriesContent(
            state = LotteriesState.Content(),
            tabs = previewTabs,
            tabSelected = {},
            lotteryClick = {},
            skipToBrowse = {},
        )
    }
}

/** Preview with content inside, on the Waitlisted tab. */
@Preview(showBackground = true)
@Composable
private fun LotteriesScreenPreview() {
    TimewarpTheme {
        LotteriesContent(
            state = LotteriesState.Content(
                selectedTab = LotteryTab.Waitlisted,
                items = listOf(
                    LotteryItem("1", ReservationState.Waitlisted, "Super Cool Lottery", "3 Winners - 50 People Waiting", "Drawn 3 days ago"),
                    LotteryItem("2", ReservationState.Waitlisted, "Super Cool Lottery", "3 Winners - 50 People Waiting", "Drawn 3 days ago"),
                ),
            ),
            tabs = previewTabs,
            tabSelected = {},
            lotteryClick = {},
            skipToBrowse = {},
        )
    }
}