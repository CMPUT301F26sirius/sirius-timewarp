package com.example.timewarp.feature.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val TEST_USER_ID: String? = null

/**
Main Responsibility: Responsible for holding all the text that can change whenever
                     the user switches between tabs.

Parameters: @param label: Text shown on the tab chip
            @param heading: Heading shown above the list
            @param emptyHeading: Heading shown when the list has no value
            @param  cardSubtitle: Small text under each card's title
            @param  datePrefix: Text placed before the "_ days ago"
 */

sealed class ReservationTab(
    val label: String,
    val heading: String,
    val emptyHeading: String,
    val cardSubtitle: String,
    val datePrefix: String,
) {
    // Reservations that are confirmed/upcoming
    data object Booked : ReservationTab(
        label = "Booked",
        heading = "Get ready to go!",
        emptyHeading = "Nothing booked yet!",
        cardSubtitle = "Tap to manage your reservation",
        datePrefix = ""
    )

    // Lotteries that the user is either in or won, but does not hold a current reservation
    data object Booking : ReservationTab(
        label = "Booking",
        heading = "There is still a chance!",
        emptyHeading = "You haven't booked yet!",
        cardSubtitle = "Make your reservation now!",
        datePrefix = ""
    )

    // Past reservations
    data object Past : ReservationTab(
        label = "Past",
        heading = "Trips you've taken",
        emptyHeading = "No past trips yet!",
        cardSubtitle = "Hope you had a great trip",
        datePrefix = ""
    )

    // Any reservations that were canceled either by the user or the owner
    data object Cancelled : ReservationTab(
        label = "Cancelled",
        heading = "Cancelled bookings",
        emptyHeading = "Nothing cancelled!",
        cardSubtitle = "This booking was cancelled",
        datePrefix = ""
    )
}

/**
Main Responsibility: Responsible for deciding which tabs appear and which tabs are default.
                     Which reservation each mode may show. To add a new view, add it in here
                     to apply filtering and different view selection

Parameters: @param tabs: Tabs shown at the top of the screen
            @param defaultTab: the tab that is selected on opening
 */
sealed class ReservationMode(
    val tabs: List<ReservationTab>,
    val defaultTab: ReservationTab
){

    // Entrant viewing their reservations
    data object Entrant: ReservationMode(
        tabs = listOf(ReservationTab.Booked, ReservationTab.Booking, ReservationTab.Past,
            ReservationTab.Cancelled
        ),
        defaultTab = ReservationTab.Booking
    )

    // TODO("data object Owner")
    // TODO("data Object Admin")

}

/**
Main Responsibility: Temp fun. Right now it's to match different ways of saying the same thing,
                     will later change.
 */
fun ReservationState.toTab(): ReservationTab = when (this) {
    ReservationState.Waitlisted,              // in the lottery, still a chance
    ReservationState.Pending -> ReservationTab.Booking   // won, needs to make the reservation

    ReservationState.Reserved -> ReservationTab.Booked   // confirmed

    ReservationState.Declined,                // user turned down the spot
    ReservationState.Canceled,                // reservation cancelled
    ReservationState.Rejected -> ReservationTab.Cancelled // not accepted
    }

/**
Main Responsibility: Responsible for holding only what each card is supposed to show.

Parameters: @param reservationId: Used to open the reservation when the card is tapped
            @param which tab the card appears in
            @param title: Campsite name
            @param dateText: text such as "Won 2 days ago"
            @param canMakeReservation: True when the user won and needs to reserve.
 */
data class ReservationItem(
    val reservationId: String,
    val tab: ReservationTab,
    val title: String,
    val dateText: String,
    val canMakeReservation: Boolean = false,
)

// interface for everything reservation screen is allowed to show
sealed interface ReservationsState {
    // Reservations that are being fetched (aka loading.)
    data object Loading : ReservationsState

    /**
    Main Responsibility: Responsible for making sure that reservations load successfully or if
                         they are empty.

    Parameters: @param tabs: Which tabs to show for each mode
                @param selectedTab: the tab which the user currently clicked
                @param items: Every reservation for this tab
     */
    data class Content(
        val tabs: List<ReservationTab> = ReservationMode.Entrant.tabs,
        val selectedTab: ReservationTab = ReservationMode.Entrant.defaultTab,
        val items: List<ReservationItem> = emptyList(),
    ) : ReservationsState {
        // Only the reservations belonging to the current tab (selectedTab)
        val visibleItems: List<ReservationItem>
            get() = items.filter { it.tab == selectedTab }
    }

    // Could not load, send error
    data class Error(val message: String) : ReservationsState
    }

/**
Main Responsibility: Responsible for loading reservations that each mode can see, combines
                     the campsite and turns it into a card.

Parameters: @param campsiteRepository: campsite information (mainly used for campsite name)
            @param reservationRepository: Reservation information
            @param observeCurrentUser: Signed-in userId
 */
@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val reservationRepository: ReservationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationsState>(ReservationsState.Loading)

    // Current Screen State
    val state: StateFlow<ReservationsState> = _state.asStateFlow()

    /**
    Main Responsibility: Responsible for loading the screen. So if the state changes such as a
                         cancellation, these changes appear when coming back to reservations.

    Parameters: @param mode: Which user (Entrant, Owner, Admin) is viewing and what tabs and reservations
                         are shown
     */
    fun load(mode: ReservationMode = ReservationMode.Entrant) {

        viewModelScope.launch {
            // remembers current tab
            val selectedTab = (_state.value as? ReservationsState.Content)?.selectedTab ?: mode.defaultTab

            // Only shows "Loading..." at the beginning
            if (_state.value !is ReservationsState.Content) _state.value = ReservationsState.Loading

            try {
                // Use the test ID if not null, otherwise use signe-in User
                val userId = TEST_USER_ID ?: observeCurrentUser().first()?.userId
                if (userId == null) {
                    _state.value = ReservationsState.Error("Sign in to view reservations")
                    return@launch
                }

                // Filtering for reservations
                val allReservations = reservationRepository.getReservations().orEmpty()

                // Each mode decides which reservations to show
                val reservations = when (mode) {
                    ReservationMode.Entrant -> allReservations.filter { it.userId == userId }

                    // TODO ("ReservationMode.Owner --> reservations for which they own")
                    // TODO ("ReservationMode.Admin --> allReservations")

                }.sortedByDescending { it.timestamp }


                val campsiteNames = reservations
                    .map { it.campsiteId }
                    .distinct()
                    .associateWith { campsiteId -> campsiteRepository.getCampsite(campsiteId)?.name }

                val items = reservations.map { it.toItem(campsiteNames[it.campsiteId]) }

                _state.value = ReservationsState.Content(tabs = mode.tabs, selectedTab = selectedTab, items = items)
            } catch (e: Exception) {
                _state.value = ReservationsState.Error("Unable to load reservations")
            }
        }
    }

    /**
    Main Responsibility: Turning a reservation into a card

    Parameters: @param campsiteName: Name of campsite
     */
    private fun Reservation.toItem(campsiteName: String?): ReservationItem {
        val tab = reservationState.toTab()
        val daysAgo = daysAgoText(timestamp)
        return ReservationItem(
            reservationId = reservationId,
            tab = tab,
            title = campsiteName?.takeIf { it.isNotBlank() } ?: "Campsite",
            dateText = if (daysAgo.isEmpty()) "" else tab.datePrefix + daysAgo,
            canMakeReservation = reservationState == ReservationState.Pending
        )
    }

    /**
    Main Responsibility: Switches tabs

    Parameters: @param tab: The tab the user clicked
     */
    fun selectTab(tab: ReservationTab) {
        _state.update { (it as? ReservationsState.Content)?.copy(selectedTab = tab) ?: it }
    }

    /**
    Main Responsibility: Formatting how long ago a timestamp was

    Parameters: @param epochSeconds: time to format, in seconds
     */
    private fun daysAgoText(epochSeconds: Int): String {
        if (epochSeconds <= 0) return ""

        val elapsedMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(epochSeconds.toLong())
        val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis).coerceAtLeast(0)

        return when (days) {
            0L -> "Today"
            1L -> "1 day ago"
            else -> "$days days ago"
        }
    }

}