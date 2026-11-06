package com.example.timewarp.feature.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.ui.theme.TimewarpTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.Calendar


/*
* This file holds the Filter Sheet on the Browse Screen where the users can set a location
*  or the number of guests to filter through the campsites
*/


/*
* US 01.01.02: Filtering by 6 guests. Hides teh campsite that has 4 guests
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * @param filter the filters currently applied
 * @param onApply called with the new filters when user taps Apply button or Clear All button
 * @param onDismiss called when the sheet is dismissed without applying
 */
fun FilterSheet(
   filter: CampsiteFilter,
   onApply: (CampsiteFilter) -> Unit,
   onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        FilterSheetContent(filter = filter, onApply = onApply)
    }
}


/**
 * @param filter the filters currently applied
 * @param onApply called with the new filters when user taps Apply button or Clear All button
 * @param modifier modifier for the form column
 */
@Composable
fun FilterSheetContent(
    filter: CampsiteFilter,
    onApply: (CampsiteFilter) -> Unit,
    modifier: Modifier = Modifier
    ) {

    var location by remember(filter){ mutableStateOf(filter.location) }
    var guests by remember(filter){ mutableStateOf(filter.guests) }
    var startDate by remember(filter) { mutableStateOf(filter.startDate) }
    var endDate by remember(filter) { mutableStateOf(filter.endDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") }
    }

    val start = startDate
    val end = endDate
    val dateLabel = if (start != null && end != null) {
        "${dateFormat.format(Date(start))} - ${dateFormat.format(Date(end))}"
    }
    else {
        "Select Dates"
    }

    Column(
        modifier = modifier.padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Filters")

        OutlinedTextField(
            value = location,
            onValueChange = {location = it},
            modifier = Modifier.fillMaxWidth().testTag("filter_location"),
            label = {Text("Location")},
            singleLine = true
        )

        // Dates selector
        OutlinedButton(
            onClick = {showDatePicker = true},
            modifier = Modifier.fillMaxWidth().testTag("filter_dates")
        ) {
            Text(dateLabel)
        }

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
                    onClick = {
                        guests = if (guests == null || guests!! <= 1) null
                        else guests!! - 1
                    }
                ) {
                    Text("-")
                }

                Text(guests?.toString() ?: "Any")

                OutlinedButton(
                    onClick = { guests = (guests ?: 0) + 1 },
                    modifier = Modifier.testTag("filter_guests_plus")
                ) {
                    Text("+")
                }
            }
        }

        // Button for Clear All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {onApply(CampsiteFilter())},
                modifier = Modifier.weight(1f).testTag("filter_clear")
            ) {
                Text("Clear All")
            }

            // Button for Apply Filter
            Button(
                onClick = {onApply(filter.copy(location = location.trim(), guests = guests,
                    startDate = startDate, endDate = endDate))},
                modifier = Modifier.weight(1f).testTag("filter_apply")
            ) {
                Text("Apply")
            }
        }
    }

    // This handles the card that shows a calendar to pick dates.
    if (showDatePicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate)

        // This function gives an option to move the calendar to next month
        fun moveMonth(by: Int) {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = pickerState.displayedMonthMillis
            calendar.add(Calendar.MONTH, by)
            pickerState.displayedMonthMillis = calendar.timeInMillis
        }

        DatePickerDialog(
            onDismissRequest = {showDatePicker = false},
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null &&
                            pickerState.selectedEndDateMillis != null,
                    onClick = {
                        startDate = pickerState.selectedStartDateMillis
                        endDate = pickerState.selectedEndDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {showDatePicker = false}
                ) {
                    Text("Cancel")
                }
            }
        )
        {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(
                        start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Select Dates", style = MaterialTheme.typography.labelLarge)

                    Row {
                        IconButton(
                            onClick = {moveMonth(-1)},
                            modifier = Modifier.testTag("filter_previous_month")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month" )
                        }
                        IconButton(
                            onClick = {moveMonth(1)},
                            modifier = Modifier.testTag("filter_next_month")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month" )
                        }
                    }
                }

                DateRangePicker(
                    state = pickerState,
                    modifier = Modifier.height(500.dp),
                    title = null,
                    headline = {
                        DateRangePickerDefaults.DateRangePickerHeadline(
                            selectedStartDateMillis = pickerState.selectedStartDateMillis,
                            selectedEndDateMillis = pickerState.selectedEndDateMillis,
                            displayMode = pickerState.displayMode,
                            dateFormatter = remember { DatePickerDefaults.dateFormatter() },
                            modifier = Modifier.padding(start = 20.dp, end = 12.dp, bottom = 12.dp)
                        )
                    }
                )
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