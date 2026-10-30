package com.example.timewarp.feature.campsite.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun CampsiteDetailsScreen(
    campsiteId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.CampsiteDetails,
    navController: NavController = rememberNavController(),
    viewModel: CampsiteDetailsViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(campsiteId) {
        viewModel.observe(campsiteId)
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            when (val currentState = state) {
                CampsiteDetailsState.Loading -> Text("Loading campsite details...")
                is CampsiteDetailsState.Error -> Text("Error: ${currentState.message}")

                is CampsiteDetailsState.Content -> {
                    // Campsite
                    val campsite = currentState.campsite
                    Text(text = "Name: ${campsite.name}")
                    Text(text = "Description: ${campsite.description}")
                    Text(text = "Address: ${campsite.location.address}")
                    Text(text = "Rating: ${campsite.rating}")
                    Text(text = "Amenities: ${campsite.amenities.joinToString()}")
                    Text(text = "Tags: ${campsite.tags.joinToString()}")

                    // Owner info
                    Text("Owner")
                    Text(text = currentState.ownerUser?.let { "User ID: ${it.userId}, Role: ${it.userType}" } ?: "Owner information unavailable")
                    currentState.ownerProfile?.let {
                        Text(text = "Profile: ${it.firstName} ${it.lastName}\nEmail: ${it.email}\nPhone: ${it.phoneNumber}")
                    }

                    // Your type
                    Text(text = "Viewing as: ${currentState.currentUser?.userType ?: "Signed out"}")

                    // Ratings
                    Text(text = "Ratings (${currentState.ratings.size})")
                    if (currentState.ratings.isEmpty()) {
                        Text(text = "No ratings yet")
                    } else {
                        currentState.ratings.forEach {
                            Text(text = "${it.rating}/5 - ${it.review}")
                        }
                    }

                    // Lotteries
                    Text(text = "Lotteries (${currentState.lotteries.size})")
                    if (currentState.lotteries.isEmpty()) {
                        Text(text = "No lotteries")
                    } else {
                        currentState.lotteries.forEach {
                            Text(text = "${it.lotteryId}: ${it.winnerCount} winners, ${it.waitlistCapacity} waitlist spaces")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteDetailsPreview() {
    TimewarpTheme() {
        CampsiteDetailsScreen("0")
    }
}