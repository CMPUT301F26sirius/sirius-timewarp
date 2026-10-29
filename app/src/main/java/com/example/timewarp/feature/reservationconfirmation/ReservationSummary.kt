
package com.example.timewarp.feature.reservationconfirmation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Entrant
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import kotlin.String
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun ReservationSummary(
    confirmation: ReservationConfirmation,
    modifier: Modifier = Modifier
) {
    val details = listOf(
        "Reservation #" to confirmation.reservationId,
        "Booked on" to confirmation.reservationTimestamp,
        "Check-in" to confirmation.reservationStartDate,
        "Check-out" to confirmation.reservationEndDate,
        "Booked by" to confirmation.entrantName,
        "Campsite" to confirmation.campsiteName,
        "Location" to confirmation.campsiteLocation,
    )

    Card( modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            details.forEach { (label, value) ->
                Column {
                    Text (
                        text = "${label}: ",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text (
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


