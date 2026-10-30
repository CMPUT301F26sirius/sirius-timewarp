package com.example.timewarp.feature.campsite.list_deprecated

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite

@Composable
fun CampsiteSummary (
    campsite: Campsite,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Text(
                text = campsite.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = campsite.location.address,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Maximum Occupancy: ${campsite.occupancy.maxGuests}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Rating: ${campsite.rating}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryPreview() {
//    CampsiteSummary(
//        campsite = Campsite(
//            id = "1",
//            ownerId = "",
//            name = "Townsite Campground",
//            description = "A quite Campground for families",
//            location = Location("Waterton Park, Alberta"),
//            rating = 3.7
//        )
//    )
}