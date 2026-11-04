package com.example.timewarp.feature.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.ui.theme.TimewarpTheme


/*
* This file holds the Filter Sheet on the Browse Screen where the users can set a location
*  or the number of guests to filter through the campsites
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
   filter: CampsiteFilter,
   onApply: (CampsiteFilter) -> Unit,
   onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        FilterSheetContent(filter = filter, onApply = onApply)
    }
}

@Composable
fun FilterSheetContent(
    filter: CampsiteFilter,
    onApply: (CampsiteFilter) -> Unit,
    modifier: Modifier = Modifier
    ) {

    var location by remember(filter){ mutableStateOf(filter.location) }
    var guests by remember(filter){ mutableStateOf(filter.guests) }

    Column(
        modifier = modifier.padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Filters")

        OutlinedTextField(
            value = location,
            onValueChange = {location = it},
            modifier = Modifier.fillMaxWidth(),
            label = {Text("Location")},
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Guests")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {guests = if (guests == null || guests !! <= 1) null
                    else guests!! -1}
                ) {
                    Text("-")
                }

                Text(guests?.toString() ?: "Any")

                OutlinedButton(
                    onClick = {guests = (guests?: 0) + 1}
                ) {
                    Text("+")
                }
            }

            // Button for Clear All
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {onApply(CampsiteFilter())},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }

                // Button for Apply Filter
                Button(
                    onClick = {onApply(filter.copy(location = location.trim(), guests = guests))},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun FilterSheetContentPreview() {
    TimewarpTheme {
        FilterSheetContent(filter = CampsiteFilter(location = "Jasper", guests = 4),
            onApply = {}
        )
    }
}