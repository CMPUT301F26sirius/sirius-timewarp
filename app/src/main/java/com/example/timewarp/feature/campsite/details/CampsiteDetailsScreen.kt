package com.example.timewarp.feature.campsite.details
import androidx.compose.foundation.Image
import com.example.timewarp.R
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        //Replace with actual image from firebase
                        Image(
                            painter = painterResource(R.drawable.maligne_lake),
                            contentDescription = "Campsite photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = campsite.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = campsite.location.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.StarOutline,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = campsite.rating.toString(),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${currentState.ratings.size} reviews",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }




                    }

                    //Check if owner
                    //val isOwner = currentState.currentUser?.userId == campsite.ownerUserId &&
                            //currentState.currentUser != null

                    if (true) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    // TODO: Open edit campsite screen
                                },
                                icon = Icons.Outlined.Edit,
                                text = "Edit campsite",

                                modifier = Modifier.fillMaxWidth()
                            )

                            BaseButton(
                                onClick = {
                                    // TODO: Generate QR code
                                },
                                text = "Generate QR Code",
                                modifier = Modifier.fillMaxWidth()
                            )

                            BaseButton(
                                onClick = {
                                    // TODO: Open create lottery screen
                                },
                                text = "Create lottery",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }








                    Text(text = "Description: ${campsite.description}")


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