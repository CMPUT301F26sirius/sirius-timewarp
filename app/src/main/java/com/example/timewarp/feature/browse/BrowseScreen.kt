package com.example.timewarp.feature.browse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.CampsiteCard
import com.example.timewarp.ui.theme.TimewarpTheme
import com.example.timewarp.feature.campsite.owner.CampsiteListToggle
import com.example.timewarp.feature.campsite.owner.MyCampsitesContent
import com.example.timewarp.feature.campsite.owner.MyCampsitesViewModel

/*
* This file holds the Browse Screen which has the search bar, the filter button and sheet and
* the list of campsite results.
*/

@Composable
fun BrowseScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Browse,
    navController: NavController = rememberNavController(),
    viewModel: BrowseViewModel = hiltViewModel(),
    myCampsitesViewModel: MyCampsitesViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOwner by myCampsitesViewModel.isOwner.collectAsStateWithLifecycle()
    val myCampsitesState by myCampsitesViewModel.state.collectAsStateWithLifecycle()

    // show filter
    var showFilters by rememberSaveable {mutableStateOf(false)}

    // Owners can switch between all campsites and their own (US 02.03.01)
    var showMyCampsites by rememberSaveable { mutableStateOf(false) }

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
            isOwner = isOwner,
            showMyCampsites = showMyCampsites,
            onToggleMyCampsites = { showMyCampsites = it },
            myCampsites = {
                MyCampsitesContent(
                    state = myCampsitesState,
                    onCampsiteClick = { id ->
                        navController.navigate(Screen.CampsiteDetails.createRoute(id))
                    }
                )
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

@Composable
fun BrowseContent(
    state: BrowseState,
    onCampsiteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    query: String,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    isOwner: Boolean = false,
    showMyCampsites: Boolean = false,
    onToggleMyCampsites: (Boolean) -> Unit = {},
    myCampsites: @Composable () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchRow(query = query, onSearch = onSearch, onFilterClick = onFilterClick)

        // Owners see "All campsites" / "My campsites" pills (US 02.03.01)
        if (isOwner) {
            CampsiteListToggle(showMyCampsites = showMyCampsites, onToggle = onToggleMyCampsites)
        }

        // "My campsites" selected: show the owner's list instead of search results
        if (isOwner && showMyCampsites) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 15.dp)) {
                myCampsites()
            }
            return@Column
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
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
                    modifier = Modifier.fillMaxSize(),
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

@Composable
private fun SearchRow(
    query: String,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    var text by rememberSaveable(query) { mutableStateOf(query) }

    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 8.dp),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {

        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {onSearch(text)}
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
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

            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer).clickable(onClick = onFilterClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Filters")
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


