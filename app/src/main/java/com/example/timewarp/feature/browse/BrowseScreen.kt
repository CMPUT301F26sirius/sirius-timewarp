package com.example.timewarp.feature.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.timewarp.ui.component.IconButton
import com.example.timewarp.ui.theme.TimewarpTheme
import com.google.firebase.annotations.concurrent.Background

@Composable
fun BrowseScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Browse,
    navController: NavController = rememberNavController(),
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

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
        )
    }
}

@Composable
fun BrowseContent(
    state: BrowseState,
    onCampsiteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
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
            onCampsiteClick = {}
        )
    }
}


//    val scrollState = rememberScrollState()
//
//    // State Interpretation & Navigation
//    LaunchedEffect(state) {
//
//    }
//
//    // User Interface
//    NavigationScreen(
//        screen = screen,
//        modifier = modifier,
//        navController = navController,
//    ) {
//        IconButton(
//            text = "View Campsite Details",
//            icon = Icons.Outlined.Terrain,
//            onClick = {
//                navController.navigate(Screen.CampsiteDetails.createRoute("0"))
//            },
//        )
//    }
//}
//


