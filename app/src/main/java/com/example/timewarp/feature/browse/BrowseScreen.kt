package com.example.timewarp.feature.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.CampsiteCard
import com.example.timewarp.ui.theme.TimewarpTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType

/*
* This file holds the Browse Screen which has the search bar, the filter button and sheet and
* the list of campsite results.
*/


/**
 * This function runs the Browse Screen: search bar, filter sheet and list of campsites
 * @param modifier modifier for the screen
 * @param screen the navigation entry this screen belongs to
 * @param navController used to open the campsite details screen
 * @param viewModel holds the campsites, search text, filters and tabs
*/
@Composable
fun BrowseScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Browse,
    navController: NavController = rememberNavController(),
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // show filter
    var showFilters by rememberSaveable {mutableStateOf(false)}

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        BrowseContent(
            state = state,
            onCampsiteClick = { id ->
                navController.navigate(
                    Screen.CampsiteDetails.createRoute(id))
            },
            query = state.filters.query,
            onSearch = viewModel::onSearch,
            onFilterClick = { showFilters = true },
            onTabSelected = viewModel::onTabSelected,

            onRegisterCampsite = {
                navController.navigate(Screen.CreateCampsite.route)
            }

        )
        if (showFilters) {
            FilterSheet(filter = state.filters,
                onApply = {viewModel.onApplyFilters(it)
                showFilters = false}, onDismiss = {showFilters = false}
            )
        }
    }
}

/**
 * This function holds the search card and the campsite list. It has loading, error and empty state.
 * @param state the campsites, filters, tabs, loading and error status to display
 * @param onCampsiteClick called with the campsite id when a card is tapped
 * @param modifier modifier for the root column
 * @param query the search which is currently being applied
 * @param onSearch called with the search text when the user searches something
 * @param onFilterClick called when the user opens teh filter sheet
 * @param showFilterButton hides the filter button and teh dates and guests summary
 * @param onTabSelected called with the tab the user taps
 * @param onRegisterCampsite called when an owner chooses to register a campsite
*/
@Composable
fun BrowseContent(
    state: BrowseState,
    onCampsiteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    query: String,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    showFilterButton: Boolean = true,
    onTabSelected: (BrowseTab) -> Unit = {},

    onRegisterCampsite: () -> Unit = {}

) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchRow(query = query, onSearch = onSearch, filter = state.filters, onFilterClick = onFilterClick,
            showFilterButton = showFilterButton
        )

        if (state.tabs.size > 1) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                state.tabs.forEach {
                    tab ->
                    FilterChip(
                        selected = tab == state.selectedTab,
                        onClick = {onTabSelected(tab)},
                        label = {Text(tab.label)},
                        modifier = Modifier.testTag("browse_tab_${tab.key}")
                    )
                }
            }
        }


        //Check if owner type and show register campsite button
        val isOwnerType =
            state.currentUser?.userType == UserType.CampsiteOwner

        if (isOwnerType) {
            BaseButton(
                text = "Register campsite",
                onClick = onRegisterCampsite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 8.dp)
                    .testTag("register_campsite_button")
            )
        }



        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {

                state.selectedTab == BrowseTab.MyCampsites -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.campsites.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Your Added Campsites Will Appear Here")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(vertical = 15.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(
                                    items = state.campsites,
                                    key = { campsite -> campsite.campsiteId }
                                ) { campsite ->
                                    CampsiteCard(
                                        campsite = campsite,
                                        onClick = { onCampsiteClick(campsite.campsiteId) }
                                    )
                                }
                            }
                        }
                    }
                }

                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                state.error != null ->
                    Text(
                        text = state.error ?: "An Error Occurred",
                        modifier = Modifier.align(Alignment.Center)
                    )

                state.campsites.isEmpty() ->
                    Text(
                        text = "No Matches Found",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("browse_list"),
                    contentPadding = PaddingValues(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.campsites,
                        key = { campsite -> campsite.campsiteId }
                    ) { campsite ->
                        CampsiteCard(
                            campsite = campsite,
                            onClick = { onCampsiteClick(campsite.campsiteId) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * This function shows a search card at the top of browse screen. It has search fields and filter
 * button with the chosen dates and guests underneath. Clicking the summary row opens this filter
 * @param query the search which is currently being applied
 * @param filter the active filters used for the dates and guests summary
 * @param onSearch called with the search text when the user searches something
 * @param onFilterClick called when the user opens teh filter sheet
 * @param showFilterButton hides the filter button and teh dates and guests summary
*/
@Composable
private fun SearchRow(
    query: String,
    filter: CampsiteFilter,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    showFilterButton: Boolean = true
) {
    var text by rememberSaveable(query) { mutableStateOf(query) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") }
    }

    val start = filter.startDate
    val end = filter.endDate
    val dateText = if (start != null && end != null) {
        "${dateFormat.format(Date(start))} - ${dateFormat.format(Date(end))}"
    }
    else {
        "Any Dates"
    }
    val guestText = filter.guests?.let{"$it guests"} ?: "Any Guests"

    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 8.dp),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {

        Column {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {onSearch(text)},
                    modifier = Modifier.testTag("browse_search_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search")

                }
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f).testTag("browse_search_field"),
                    label = { Text("Where to?") },
                    singleLine = true,
                    placeholder = { Text("Search Campsites") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {onSearch(text)}),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )

                if (showFilterButton) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer).clickable(onClick = onFilterClick)
                            .testTag("browse_filter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Filters")
                    }
                }
        }
            if (showFilterButton) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onFilterClick).padding(
                        horizontal = 15.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(9.dp))
                        Text(dateText)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, null, Modifier.size(15.dp))
                        Spacer(Modifier.width(9.dp))
                        Text(guestText)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BrowseContentPreview() {
    TimewarpTheme {
        BrowseContent(
            state = BrowseState(
                campsites = listOf(
                    Campsite(
                        campsiteId = "1",
                        name = "Bear Creek Campground",
                        rating = 4.6,
                        location = Location(address = "Dryton Valley, AB"),
                        occupancy = Occupancy(maxGuests = 8)
                    )
                ),
                isLoading = false
            ),
            onCampsiteClick = {},
            query = "",
            onSearch = {},
            onFilterClick = {}
        )
    }
}
