package com.example.timewarp.feature.campsite.details

import android.R.attr.text
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
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
import com.example.timewarp.ui.component.RemoteImage
import com.example.timewarp.ui.theme.TimewarpTheme
import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.timewarp.domain.model.Rating
import java.util.concurrent.TimeUnit
import com.example.timewarp.core.util.formatPrice


/**
 * [CampsiteDetailsScreen]
 *
 * Displays details, including active lotteries for the campsite.
 */
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
                    val today =
                        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()).toInt()
                    val activeLotteries = currentState.lotteries.filter { lottery ->
                        today in lottery.registrationPeriod.start..lottery.registrationPeriod.end
                    }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val campsiteImage = currentState.campsiteImage?.takeIf {
                        it.imageId.isNotBlank() && it.url.isNotBlank()
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        val campsiteImageModifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                        if (campsiteImage != null) {
                            RemoteImage(
                                image = campsiteImage,
                                modifier = campsiteImageModifier.testTag(
                                    "details_campsite_remote_image"
                                )
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.maligne_lake),
                                contentDescription = "Campsite photo",
                                modifier = campsiteImageModifier.testTag(
                                    "details_campsite_image"
                                ),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Text(
                            text = campsite.name,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("details_name")
                        )

                        Text(
                            text = campsite.location.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("details_address")
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
                                text = "%.1f".format(currentState.averageRating),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("details_average_rating")
                            )

                            Text(
                                text = "${currentState.ratingCount} reviews",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("details_rating_count")
                            )
                        }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().testTag("details_map")
                                .clickable{
                                    /*
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(campsite.location.address)}")
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                    */
                                    val latitude = campsite.location.latitude
                                    val longitude = campsite.location.longitude
                                    val  hasCords = latitude != 0.0 || longitude != 0.0
                                    val uri = if(hasCords) {
                                        Uri.parse("geo:$latitude, $longitude?q = $latitude, $longitude" +
                                                "(${Uri.encode(campsite.name)})")
                                    } else {
                                        Uri.parse("geo:0,0?q=${Uri.encode(campsite.location.address)}")
                                    }
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                                Column {
                                    Text(campsite.location.address)
                                    Text("Tap To Open In Maps", style = MaterialTheme.typography.labelSmall)
                                }
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
                                        navController.navigate(Screen.EditCampsite.createRoute(campsiteId))
                                    },
                                    icon = Icons.Outlined.Edit,
                                    text = "Edit campsite",

                                    modifier = Modifier.fillMaxWidth()
                                )

                                BaseButton(
                                    onClick = {
                                        navController.navigate(
                                            Screen.GenerateQR.createRoute(
                                                "pitch://${Screen.CampsiteDetails.createRoute(campsiteId)}"
                                            )
                                        )
                                    },
                                    text = "Generate QR Code",
                                    modifier = Modifier.fillMaxWidth()
                                )

                                BaseButton(
                                    onClick = {
                                        navController.navigate(
                                            Screen.CreateLottery.createRoute(campsiteId)
                                        )
                                    },
                                    text = "Create lottery",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
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
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Rates and occupancy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val pricing = campsite.pricing

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Price per night")
                            Text(
                                text = formatPrice(pricing.costPerNight, pricing.currencyCode),
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
                                text =  formatPrice(pricing.securityDeposit, pricing.currencyCode),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Preparation fee")
                            Text(
                                text =  formatPrice(pricing.preparationFee, pricing.currencyCode),
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
                            .testTag("details_rules"),
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
                            .fillMaxWidth(),
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
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Lotteries",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (activeLotteries.isEmpty()) {
                            Text("No lotteries available")
                        } else {
                            activeLotteries.forEach { lottery ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("lottery_card_${lottery.lotteryId}"),
                                    onClick = {
                                        navController.navigate(Screen.LotteryDetails.createRoute(lottery.lotteryId))
                                    }
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
                        var editing by remember {mutableStateOf(false)}
                        if (currentState.canRate && (currentState.myRating == null || editing)) {
                            RatingInput(
                                myRating = currentState.myRating,
                                onSubmit = {
                                    stars, review -> viewModel.onSubmitRating(stars,review)
                                    editing = false
                                },
                                onCancel = if (editing) ({editing = false})
                                else null
                            )
                            HorizontalDivider()
                        }

                        Text(
                            text = "Reviews (${currentState.ratings.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (currentState.ratings.isEmpty()) {
                            Text("No reviews yet")
                        } else {
                            currentState.ratings.sortedByDescending {
                                it.userId == currentState.currentUser?.userId
                            }.forEach { rating ->
                                val isMine = rating.userId == currentState.currentUser?.userId
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isMine) {
                                            Text(
                                                text = "Your Review",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = "${rating.rating}/5 ★",
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        if (rating.review.isNotBlank()){
                                            Text(text = rating.review,
                                                modifier = Modifier.testTag("review_item"))
                                        }

                                        if (isMine && currentState.canRate && !editing) {
                                            TextButton(
                                                onClick = {editing = true},
                                                modifier = Modifier.testTag("edit_rating_button")
                                            ) {
                                                Text("Edit")
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
    }
}


@Composable
private fun RatingInput (
    myRating: Rating?,
    onSubmit: (stars: Int, review: String) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
) {
    var stars by remember (myRating) { mutableStateOf(myRating?.rating?: 0) }
    var review by remember (myRating) { mutableStateOf(myRating?.review?: "") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (myRating == null) "Rate This Campsite"
            else "Your Ratings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // This is for the rating stars between 1 - 5
        Row {
            (1..5).forEach {
                rate ->
                Icon(
                    imageVector = if (rate <= stars) Icons.Filled.Star
                    else Icons.Outlined.StarOutline,
                    contentDescription = "$rate stars",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp).clickable{stars = rate}.testTag("rating_star_$rate")

                )
            }
        }
        OutlinedTextField(
            value = review,
            onValueChange = {review = it},
            label = {Text("Please leave a review (Optional)")},
            modifier = Modifier.fillMaxWidth().testTag("review_field")
        )

        Button(
            onClick = {onSubmit(stars, review)},
            enabled = stars in 1..5,
            modifier = Modifier.fillMaxWidth().testTag("submit_rating_button")
        ) {
            Text(
                if (onCancel == null) "Submit"
                else "Update"
            )
        }

            if (onCancel != null) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().testTag("cancel_edit_button")
                ) {
                    Text("Cancel")
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