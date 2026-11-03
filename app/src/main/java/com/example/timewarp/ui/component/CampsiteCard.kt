package com.example.timewarp.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy

@Composable
fun CampsiteCard(
    campsite: Campsite, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable{onClick()},
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.dp)
        ) {
            Text(text = campsite.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = campsite.location.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Maximum Occupancy: ${campsite.occupancy.maxGuests}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Rating: %.1f".format(campsite.rating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampsitePreview() {
    CampsiteCard(
        campsite = Campsite(
            campsiteId = "1",
            name = "Bear Creek Campground",
            rating = 5.0,
            location = Location(address = "Dryton Valley, AB"),
            occupancy = Occupancy(maxGuests = 8)
        ),
        onClick = {}
    )
}