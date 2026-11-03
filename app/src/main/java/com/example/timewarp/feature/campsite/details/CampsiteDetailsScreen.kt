package com.example.timewarp.feature.campsite.details
import androidx.compose.foundation.Image
import com.example.timewarp.R
import androidx.compose.ui.graphics.Color

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
                    val isOwner = currentState.currentUser?.userId == campsite.ownerUserId &&
                            currentState.currentUser != null

                    if (isOwner) {
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













                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Description
                        Text(
                            text = "Wake up by the water",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = campsite.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider()

                        // Amenities
                        Text(
                            text = "The essentials, covered",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (campsite.amenities.isEmpty()) {
                            Text("No amenities listed")
                        } else {
                            campsite.amenities.chunked(2).forEach { rowAmenities ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowAmenities.forEach { amenity ->
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = amenity,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }

                                    if (rowAmenities.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }



                    //Tags

                    if (campsite.tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            campsite.tags.forEach { tag ->
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text(text = tag)
                                    }
                                )
                            }
                        }
                    }

                    //Rates and Occupancy
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Rates and occupancy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Price per night")
                            Text(
                                text = "${campsite.pricing.costPerNight} ${campsite.pricing.currencyCode}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Maximum guests")
                            Text(
                                text = "${campsite.occupancy.maxGuests}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Security deposit")
                            Text(
                                text = "${campsite.pricing.securityDeposit} ${campsite.pricing.currencyCode}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Preparation fee")
                            Text(
                                text = "${campsite.pricing.preparationFee} ${campsite.pricing.currencyCode}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    //Rules and Cancellation Policy
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Campsite rules",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (campsite.rules.isEmpty()) {
                            Text("No rules specified")
                        } else {
                            campsite.rules.forEach { rule ->
                                Text(
                                    text = "• $rule",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Text(
                            text = "Cancellation policy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = campsite.cancellationPolicy.policy
                                .ifBlank { "Not specified" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    //Owner Info

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hosted by",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val ownerName = currentState.ownerProfile?.let {
                            "${it.firstName} ${it.lastName}".trim()
                        }

                        Text(
                            text = ownerName?.takeIf { it.isNotBlank() }
                                ?: "Host information unavailable",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    //Lotteries and Reviews

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Lotteries",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (currentState.lotteries.isEmpty()) {
                            Text("No lotteries available")
                        } else {
                            currentState.lotteries.forEach { lottery ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Lottery",
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text("Winners: ${lottery.winnerCount}")
                                        Text("Waitlist spaces: ${lottery.waitlistCapacity}")
                                    }
                                }
                            }
                        }

                        HorizontalDivider()

                        //Ratings

                        Text(
                            text = "Reviews (${currentState.ratings.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (currentState.ratings.isEmpty()) {
                            Text("No reviews yet")
                        } else {
                            currentState.ratings.forEach { rating ->
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${rating.rating}/5 ★",
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(text = rating.review)
                                    }
                                }
                            }
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
        CampsiteDetailsScreen(
            campsiteId = "0",
            viewModel = hiltViewModel()
        )
    }
}