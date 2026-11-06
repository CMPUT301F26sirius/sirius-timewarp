// Parts of this code were made with the assistance of Claude AI
// While no functionally was implemented wholly by the AI
// I prompted it with questions about how to do certain things in Kotlin/Android
// Or assistance in debugging

package com.example.timewarp.feature.lottery.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.example.timewarp.navigation.Screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Period
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.theme.PitchCream
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.TimewarpTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Screen(s) for CreateLottery
 *
 * See CreateLotteryViewModel for params/properties
 */
@Composable
fun CreateLotteryScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.CreateLottery,
    navController: NavController = rememberNavController(),
    viewModel: CreateLotteryViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state is CreateLotteryState.Details) {
        viewModel.goToCampsiteSelection()
    }

    NavigationScreen (
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val currentState = state) {

                CreateLotteryState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            color = PitchCream,
                            trackColor = Color.Transparent,
                            strokeWidth = 4.dp,
                        )
                    }
                }
                is CreateLotteryState.Error -> Text("Error: ${currentState.message}")

                // Choose Campsites
                is CreateLotteryState.Content -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            CampsiteCardList(
                                campsites = currentState.campsites,
                                selectedCampsites = currentState.selectedCampsites,
                                onToggleCampsite = viewModel::toggleCampsite
                            )
                        }
                        CreateLotteryDetailsButton(
                            enabled = currentState.selectedCampsites.isNotEmpty(),
                            onClick = viewModel:: goToDetailSelection,
                        )
                    }

                }

                is CreateLotteryState.Details -> {
                    LotteryDetails(
                        winnerCount = viewModel.winnerCount,
                        waitlistCount = viewModel.waitlistCount,
                        registrationPeriod = viewModel.registrationPeriod,
                        bookingPeriod = viewModel.bookingPeriod,
                        onWinnerCountChange = viewModel::onWinnerCountChange,
                        onWaitlistCountChange = viewModel::onWaitListCountChange,
                        onRegistrationPeriodChange = viewModel::onRegistrationPeriodChange,
                        onBookingPeriodChange = viewModel::onBookingPeriodChange,
                        onBack = viewModel::goToCampsiteSelection,
                        canCreate = !viewModel.isCreating &&
                                (viewModel.winnerCount ?:0) >= 1 &&
                                (viewModel.waitlistCount ?: -1) >= 0 &&
                                viewModel.registrationPeriod != null &&
                                viewModel.bookingPeriod != null,
                        createError = viewModel.createError,
                        onCreate = { viewModel.createLotteryInDB { navController.popBackStack() } },
                    )
                }
            }
        }
    }
}

/**
 * Displays all user owned campsite as a list of Card
 *
 * @param campsites List of user owned Campsites
 * @param selectedCampsites List of currently selected campsites
 * @param onToggleCampsite Add/remove campsite from list
 */
@Composable
private fun CampsiteCardList (
    campsites: List<Campsite>,
    selectedCampsites: List<Campsite>,
    onToggleCampsite: (campsite: Campsite) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (campsites.isEmpty()) {
        Text(
            text = "You don't own any campsites",
            style = MaterialTheme.typography.titleLarge
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(campsites, key = {it.campsiteId}) {campsite ->
                CampsiteCard(
                    campsite = campsite,
                    selected = campsite in selectedCampsites,
                    onToggle = {onToggleCampsite(campsite)}
                )
            }
        }
    }
}

/**
 * Displays a single Campsite as a Card
 *
 * @param campsite the campsite to list
 * @param selected weather or not campsite is currently selected
 * @param onToggle updates onToggleCampsite in CampsiteCardList
 */
@Composable
private fun CampsiteCard (
    campsite: Campsite,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,

) {
    Card (
        colors = if (selected) {
            CardDefaults.cardColors(PitchForest)
        } else {
            CardDefaults.cardColors(PitchCream)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { onToggle()}
                )
                .padding(12.dp)
        ) {
            Checkbox(checked = selected, onCheckedChange = null)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = campsite.name,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

/**
 * Button to move from campsite selection to details selection
 *
 * @param enabled whether the button can be clicked
 * @param onClick Go to lottery details on click
 */
@Composable
private fun CreateLotteryDetailsButton (
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseButton(
        text = "Create Lottery",
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
            .testTag("lottery_details_button"),
    )
}

/**
 * Button to create lottery
 *
 * @param enabled whether the button can be clicked
 * @param onClick Add Lottery to Firestore on click
 */
@Composable
private fun CreateLotteryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseButton(
        text = "Create Lottery",
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
            .testTag("lottery_create_button"),
    )
}

/**
 * Lottery Details Screen
 *
 * @param winnerCount number of winners
 * @param waitlistCount waitlist capacity
 * @param registrationPeriod period for registration
 * @param bookingPeriod period for booking
 * @param canCreate weather or not all conditions met to create lottery
 * @param createError Error to raise if creation fails
 * @param onWinnerCountChange update winnerCount on change
 * @param onWaitlistCountChange update waitlistCount on change
 * @param onRegistrationPeriodChange update Registration period on change
 * @param onBookingPeriodChange update Booking period on change
 * @param onCreate Create Lottery on create
 * @param onBack return to campsite selection
 */
@Composable
private fun LotteryDetails (
    winnerCount: Int?,
    waitlistCount: Int?,
    registrationPeriod: Period?,
    bookingPeriod: Period?,
    canCreate: Boolean,
    createError: String?,
    onWinnerCountChange: (String) -> Unit,
    onWaitlistCountChange: (String) -> Unit,
    onRegistrationPeriodChange: (Period?) -> Unit,
    onBookingPeriodChange: (Period?) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to campsites"
                )
            }
            Text(
                text = "Complete Lottery Creation",
                style = MaterialTheme.typography.titleLarge
            )
        }

        NumEntry(
            label = "Winner Count",
            value = winnerCount,
            onValueChange = onWinnerCountChange,
            min = 1,
            modifier = Modifier.testTag("winner_count_field")
        )
        NumEntry(
            label = "Waitlist Capacity",
            value = waitlistCount,
            onValueChange = onWaitlistCountChange,
            min = 0,
            modifier = Modifier.testTag("waitlist_count_field")
        )

        DatePicker(
            label = "Registration Period",
            period = registrationPeriod,
            onPeriodChange = onRegistrationPeriodChange,
            modifier = Modifier.testTag("registration_picker")
        )
        DatePicker(
            label = "Booking Period",
            period = bookingPeriod,
            onPeriodChange = onBookingPeriodChange,
            modifier = Modifier.testTag("booking_picker")
        )
        createError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        CreateLotteryButton(
            enabled = canCreate,
            onClick = onCreate,
        )
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * Selection for Date Picker (borrowed heavily from Filter Implementation)
 *
 * @param label for type of date selection
 * @param period the period of time selected
 * @param onPeriodChange update period when change
 */
@Composable
private fun DatePicker (
    label: String,
    period: Period?,
    onPeriodChange: (Period?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var startDate by remember(period) { mutableStateOf(period?.let {it.start * MILLIS_PER_DAY}) }
    var endDate by remember(period) { mutableStateOf(period?.let {it.end * MILLIS_PER_DAY}) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC") }
    }

    val start = startDate
    val end = endDate
    val dateLabel = if (start != null && end != null) {
        "${dateFormat.format(Date(start))} - ${dateFormat.format(Date(end))}"
    } else {
        "Select Dates"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text= label,
            style = MaterialTheme.typography.labelMedium
        )
        OutlinedButton(
            onClick = {showDatePicker = true},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(dateLabel)
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDate,
            initialSelectedEndDateMillis = endDate)

        DatePickerDialog(
            onDismissRequest = {showDatePicker = false},
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedStartDateMillis != null &&
                            pickerState.selectedEndDateMillis != null,
                    onClick = {
                        val pickedStart = pickerState.selectedStartDateMillis
                        val pickedEnd = pickerState.selectedEndDateMillis
                        startDate = pickedStart
                        endDate = pickedEnd
                        if (pickedStart != null && pickedEnd != null) {
                            onPeriodChange(
                                Period(
                                    start = pickedStart.floorDiv(MILLIS_PER_DAY).toInt(),
                                    end = pickedEnd.floorDiv(MILLIS_PER_DAY).toInt()
                                )
                            )
                        }
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
            DateRangePicker(
                state = pickerState,
                modifier = Modifier.height(500.dp),

                title = {
                    Text("Select Dates",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp))
                },
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

/**
 * Numeric Entry Field
 *
 * @param label label for field
 * @param value value entered for field
 * @param onValueChange Update when value changes
 * @param min minimum value for field
 */
@Composable
private fun NumEntry (
    label: String,
    value: Int?,
    onValueChange: (String) -> Unit,
    min: Int,
    modifier: Modifier = Modifier,
) {
    BaseInput(
        label = label,
        value = value.toString(),
        onValueChange = onValueChange,
        type = BaseInputType.Integer(min = min),
        modifier = modifier,
    )
}



@Preview(showBackground = true)
@Composable
fun CreateLotteryScreenPreview() {
    TimewarpTheme {
        CreateLotteryScreen()
    }
}