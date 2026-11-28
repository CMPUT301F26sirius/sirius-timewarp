package com.example.timewarp.feature.reservation.make

import android.R.attr.enabled
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.usecase.FindBookableDaysUseCase
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.ComponentStyles
import com.example.timewarp.ui.theme.PitchError
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox

import androidx.compose.ui.semantics.Role
import com.example.timewarp.core.util.formatPrice


/*
* Make reservation screen for lottery winners: choose a campsite and dates, then Confirm or Decline (US 01.05.01, US 01.04.01, US 01.04.03).
* Days that can't be booked are greyed out in the calendar and can't be picked.
*/

/**
 * Screen where a lottery winner chooses a campsite and dates, and confirms their reservation.
 * After confirming, opens the Reservation confirmed screen.
 *
 * @param reservationId the won reservation to confirm.
 */
@Composable
fun MakeReservationScreen(
    reservationId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.MakeReservation,
    navController: NavController = rememberNavController(),
    viewModel: MakeReservationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Load the reservation when the screen opens
    LaunchedEffect(reservationId) {
        viewModel.load(reservationId)
    }

    // After confirming, open the confirmation screen
    LaunchedEffect(state) {
        val current = state
        if (current is MakeReservationState.Confirmed) {
            navController.navigate(Screen.ReservationConfirmation.createRoute(current.reservationId)) {
                // Remove this screen so Back doesn't return to an already confirmed booking
                popUpTo("${Screen.MakeReservation.route}/{id}") {
                    inclusive = true
                }
            }
        }
        // After declining, go back to the previous screen
        if (current is MakeReservationState.Declined) {
            navController.popBackStack()
        }
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        MakeReservationContent(
            state = state,
            onConfirm = { viewModel.confirm(reservationId) },
            onDeclineConfirmed = { viewModel.decline(reservationId) },

            onDatesPicked = { checkIn, checkOut ->
                viewModel.selectDates(checkIn, checkOut)},

            onCampsiteChosen = { campsiteId ->
                viewModel.selectCampsite(campsiteId) },
            onRulesAcknowledgedChange = { checked ->
                viewModel.setRulesAcknowledged(checked)}

        )
    }
}

/**
 * Body of the Make reservation screen.
 *
 * @param state what to show.
 * @param onConfirm called when the user taps Confirm.
 * @param onDatesPicked called with the check-in and check-out days picked in the calendar.
 * @param onDeclineConfirmed called after the user confirms the decline dialog.
 * @param onCampsiteChosen called with the id of the campsite picked from the menu.
 */
@Composable
fun MakeReservationContent(
    state: MakeReservationState,
    onConfirm: () -> Unit,
    onDeclineConfirmed: () -> Unit,
    onDatesPicked: (Int, Int) -> Unit,

    onRulesAcknowledgedChange: (Boolean) -> Unit,

    onCampsiteChosen: (String) -> Unit,

) {
    // Whether the calendar dialog is open
    var showPicker by remember { mutableStateOf(false) }
    // Whether the "Decline reservation?" dialog is open
    var showDeclineDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        when (state) {
            MakeReservationState.Loading -> Text("Loading reservation...")
            is MakeReservationState.Error -> Text("Error: ${state.message}", color = PitchError)
            is MakeReservationState.Confirmed -> Text("Reservation confirmed.")
            MakeReservationState.Declined -> Text("Reservation declined.")

            is MakeReservationState.Content -> {
                // Top: what was won; takes the free space and scrolls on small screens
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Campsite info, or placeholders if it couldn't be loaded
                    var campsiteName = "Unknown campsite"
                    var address = ""
                    if (state.campsite != null) {
                        campsiteName = state.campsite.name
                        address = state.campsite.location.address
                    }

                    // Big green headline so winning feels like a celebration
                    Text(
                        text = "✨ You won! ✨",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = PitchForest
                    )

                    if (state.canChooseCampsite) {
                        // Several campsites: the chosen one shows in a menu field, like a title
                        CampsiteDropdown(state = state, onCampsiteChosen = onCampsiteChosen)
                    } else {
                        // One campsite: show its name and address like before
                        Text(text = campsiteName, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = address, color = PitchMutedText)
                    }

                    DetailRow(label = "Reservation #", value = state.reservation.reservationId)
                    DetailRow(label = "Status", value = state.reservation.reservationState.name)


                    val campsite = state.campsite
                    if (campsite != null) {
                        val pricing = campsite.pricing
                        DetailRow(label = "Max guests", value = campsite.occupancy.maxGuests.toString())
                        DetailRow(
                            label = "Price per night",
                            value = formatPrice(pricing.costPerNight, pricing.currencyCode)
                        )

                        if (state.canConfirm) {
                            Text("Campsite rules", style = MaterialTheme.typography.titleMedium)
                            if (campsite.rules.isEmpty()) {
                                Text("The owner has listed no campsite rules.")
                            } else {
                                campsite.rules.forEach { rule -> Text("• $rule") }
                            }

                            Text("Cancellation policy", style = MaterialTheme.typography.titleMedium)
                            Text(campsite.cancellationPolicy.policy.ifBlank { "No cancellation policy listed." })
                        }
                    }
                }

                // Bottom: dates and Confirm stay at the bottom of the screen
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Show date picking only when there are dates to pick from (US 01.04.01)
                    if (state.needsDates) {
                        DatesSection(state = state, onChooseDates = { showPicker = true })
                    }

                    // Only won (Pending) reservations can be confirmed
                    if (state.canConfirm) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("campsite_rules_acknowledgment")
                                .toggleable(
                                    value = state.rulesAcknowledged,
                                    enabled = state.campsite != null,
                                    role = Role.Checkbox,
                                    onValueChange = onRulesAcknowledgedChange
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = state.rulesAcknowledged,
                                onCheckedChange = null,
                                enabled = state.campsite != null
                            )
                            Text("I have read the campsite rules and cancellation policy.")
                        }



                        Button(
                            onClick = onConfirm,
                            enabled = state.canSubmit,
                            shape = ComponentStyles.ButtonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ComponentStyles.ButtonHeight)
                                .testTag("confirm_reservation_button")
                        ) {
                            Text("Confirm Reservation")
                        }

                        // Changed their mind or no dates work: give the spot back
                        TextButton(
                            onClick = { showDeclineDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("decline_reservation_button")
                        ) {
                            Text("Decline reservation", color = PitchError)
                        }
                    } else {
                        Text("This reservation can't be confirmed.", color = PitchMutedText)
                    }
                }
            }
        }
    }

    // Calendar dialog, opened by the Choose dates button
    if (showPicker && state is MakeReservationState.Content && state.stayWindow != null) {
        BookingDatePicker(
            stayWindow = state.stayWindow,
            bookableDays = state.bookableDays,
            onDismiss = { showPicker = false },
            onDatesPicked = { checkIn, checkOut ->
                showPicker = false
                onDatesPicked(checkIn, checkOut)
            }
        )
    }

    // Confirmation before declining the win
    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("Decline reservation?") },
            text = { Text("Your spot will go to someone else from the lottery. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeclineDialog = false
                        onDeclineConfirmed()
                    },
                    modifier = Modifier.testTag("confirm_decline_button")
                ) {
                    Text("Yes, decline", color = PitchError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text("Keep it")
                }
            }
        )
    }
}

// The chosen campsite in a box like a form field; tapping it opens a menu of the lottery's campsites
@Composable
private fun CampsiteDropdown(state: MakeReservationState.Content, onCampsiteChosen: (String) -> Unit) {
    // Whether the menu is open
    var expanded by remember { mutableStateOf(false) }

    // Name and address shown in the box
    var chosenName = "Choose a campsite"
    var chosenAddress = ""
    if (state.campsite != null) {
        chosenName = state.campsite.name
        chosenAddress = state.campsite.location.address
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "Campsite", style = MaterialTheme.typography.labelMedium)

        // Box keeps the menu anchored under the field
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ComponentStyles.InputShape)
                    .background(ComponentStyles.InputBackgroundColor)
                    .border(ComponentStyles.InputBorderWidth, ComponentStyles.InputBorderColor, ComponentStyles.InputShape)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("campsite_dropdown")
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = chosenName, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = chosenAddress, color = PitchMutedText)
                }
                // Arrow hints that tapping opens a list
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Show campsites",
                    tint = PitchForest
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ComponentStyles.InputBackgroundColor)
            ) {
                for (campsite in state.campsites) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = campsite.name, fontWeight = FontWeight.SemiBold)
                                Text(text = campsite.location.address, color = PitchMutedText)
                            }
                        },
                        onClick = {
                            // Close the menu after a pick
                            expanded = false
                            onCampsiteChosen(campsite.campsiteId)
                        },
                        modifier = Modifier.testTag("campsite_option_${campsite.campsiteId}")
                    )
                }
            }
        }
    }
}

// Picked dates (or a hint), the total cost, an error if they don't work, and the Choose dates button
@Composable
private fun DatesSection(state: MakeReservationState.Content, onChooseDates: () -> Unit) {
    Text(text = "Your dates", style = MaterialTheme.typography.labelMedium)

    val checkIn = state.checkIn
    val checkOut = state.checkOut

    // Show the picked range, or a hint if nothing is picked yet
    var datesText = "No dates chosen yet"
    if (checkIn != null && checkOut != null) {
        datesText = "${DayFormatter.shortDate(checkIn)} – ${DayFormatter.shortDate(checkOut)}"
    }
    Text(
        text = datesText,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.testTag("chosen_dates")
    )

    // Total once valid dates are picked, e.g. "Total: 90.00 CAD for 2 nights"
    val totalCost = state.totalCost
    if (totalCost != null && state.campsite != null && checkIn != null && checkOut != null) {
        val nights = checkOut - checkIn
        var nightsText = "$nights nights"
        if (nights == 1) {
            nightsText = "1 night"
        }
        Text(
            text = "Total: ${formatPrice(totalCost, state.campsite.pricing.currencyCode)} for $nightsText",
            color = PitchForest,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag("total_cost")
        )
    }

    // Explain why Confirm is still off
    if (checkIn != null && checkOut != null && !state.datesAreValid) {
        Text("Pick at least one night, with every night available.", color = PitchError)
    }

    // Every day is taken or closed
    if (state.bookableDays.isEmpty()) {
        Text("No dates left to book at this campsite.", color = PitchMutedText)
    }

    // Green outline and text so it reads as tappable next to the filled Confirm button
    OutlinedButton(
        onClick = onChooseDates,
        enabled = state.bookableDays.isNotEmpty(),
        shape = ComponentStyles.ButtonShape,
        border = BorderStroke(1.dp, PitchForest),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PitchForest),
        modifier = Modifier
            .fillMaxWidth()
            .height(ComponentStyles.ButtonHeight)
            .testTag("choose_dates_button")
    ) {
        // Say "Change dates" once some are already picked
        var buttonText = "Choose dates"
        if (checkIn != null && checkOut != null) {
            buttonText = "Change dates"
        }
        Text(buttonText)
    }
}

// Calendar dialog; days that can't be booked are greyed out and can't be tapped
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingDatePicker(
    stayWindow: Period,
    bookableDays: List<Int>,
    onDismiss: () -> Unit,
    onDatesPicked: (Int, Int) -> Unit
) {
    // Material3 asks this for every day; false makes the day grey and untappable
    val selectableDates = remember(bookableDays) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val day = DayFormatter.fromMillis(utcTimeMillis)
                return FindBookableDaysUseCase.isSelectableDay(day, bookableDays)
            }
        }
    }

    // Open the calendar on the month the stay window starts
    val pickerState = rememberDateRangePickerState(
        initialDisplayedMonthMillis = DayFormatter.toMillis(stayWindow.start),
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedStartDateMillis != null &&
                        pickerState.selectedEndDateMillis != null,
                onClick = {
                    val startMillis = pickerState.selectedStartDateMillis
                    val endMillis = pickerState.selectedEndDateMillis
                    // Turn the picker's milliseconds into day numbers
                    if (startMillis != null && endMillis != null) {
                        onDatesPicked(DayFormatter.fromMillis(startMillis), DayFormatter.fromMillis(endMillis))
                    }
                },
                modifier = Modifier.testTag("dates_ok_button")
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = pickerState,
            modifier = Modifier.height(500.dp),
            title = {
                Text(
                    text = "Select your dates",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            }
        )
    }
}

// One label/value pair, e.g. "Status" / "Pending"
@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}



@Preview(showBackground = true, showSystemUi = true, device = "id:pixel_7")
@Composable
private fun MakeReservationContentPreview() {
    val reservation = Reservation(reservationId = "PT-271204", reservationState = ReservationState.Pending)

    // A lottery with two campsites, the first one chosen
    val campsite = Campsite(
        campsiteId = "camp-1",
        name = "Pocahontas",
        location = Location(address = "Jasper, Alberta"),
        occupancy = Occupancy(maxGuests = 6),
        pricing = Pricing(costPerNight = 45.0)
    )
    val otherCampsite = Campsite(
        campsiteId = "camp-2",
        name = "Wapiti",
        location = Location(address = "Jasper, Alberta"),
        occupancy = Occupancy(maxGuests = 4),
        pricing = Pricing(costPerNight = 38.0)
    )

    // Christmas week 2027, with Christmas Eve to Boxing Day picked
    val dec22 = 21174
    val state = MakeReservationState.Content(
        reservation = reservation,
        campsite = campsite,
        canConfirm = true,
        campsites = listOf(campsite, otherCampsite),
        stayWindow = Period(start = dec22, end = dec22 + 6),
        bookableDays = (dec22..dec22 + 6).toList(),
        checkIn = dec22 + 2,
        checkOut = dec22 + 4
    )

    TimewarpTheme {
        // Same 24dp padding and status bar space that NavigationScreen adds in the app
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            MakeReservationContent(
                state = state,
                onConfirm = {},
                onDatesPicked = { checkIn, checkOut -> },

                onRulesAcknowledgedChange = {},

                onCampsiteChosen = { campsiteId -> },
                onDeclineConfirmed = {}
            )
        }
    }
}