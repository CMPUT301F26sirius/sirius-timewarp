package com.example.timewarp.feature.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.collections.emptyList

/**
 * Responsible for holding all the text that can change whenever
 * the user switches between tabs.
 *
 *
 * @param label Text shown on the tab chip
 * @param heading Heading shown above the list
 * @param emptyHeading Heading shown when the list has no value
 * @param  cardSubtitle Small text under each card's title
 * @param  datePrefix Text placed before the "_ days ago"
 * @param states states each tab can hold
 */

sealed class ReservationTab(
    val label: String,
    val heading: String,
    val emptyHeading: String,
    val cardSubtitle: String,
    val datePrefix: String,
    val states: Set<ReservationState>,
) {
    // Reservations that are confirmed/upcoming
    data object Booked : ReservationTab(
        label = "Booked",
        heading = "Get ready to go!",
        emptyHeading = "Nothing booked yet!",
        cardSubtitle = "Tap to manage your reservation",
        datePrefix = "",
        states = setOf(ReservationState.Reserved),
    )

    // Lotteries that the user is either in or won, but does not hold a current reservation
    data object Booking : ReservationTab(
        label = "Booking",
        heading = "There is still a chance!",
        emptyHeading = "You haven't booked yet!",
        cardSubtitle = "Make your reservation now!",
        datePrefix = "",
        states = setOf(ReservationState.Pending, ReservationState.Waitlisted),
    )

    // Past reservations
    data object Past : ReservationTab(
        label = "Past",
        heading = "Trips you've taken",
        emptyHeading = "No past trips yet!",
        cardSubtitle = "Hope you had a great trip",
        datePrefix = "",
        states = emptySet(),
    )

    // Any reservations that were canceled either by the user or the owner
    data object Cancelled : ReservationTab(
        label = "Cancelled",
        heading = "Cancelled bookings",
        emptyHeading = "Nothing cancelled!",
        cardSubtitle = "This booking was cancelled",
        datePrefix = "",
        states = setOf(ReservationState.Canceled, ReservationState.Declined, ReservationState.Rejected)
    )
}

/**
 * Responsible for holding only what each card is supposed to show.
 *
 *
 * @param reservationId Used to open the reservation when the card is tapped
 * @param state which tab the card appears in
 * @param title Campsite name
 * @param dateText text such as "Won 2 days ago"
 * @param canMakeReservation True when the user won and needs to reserve.
 */
data class ReservationItem(
    val reservationId: String,
    val state: ReservationState,
    val title: String,
    val dateText: String,
    val canMakeReservation: Boolean = false,
)

// interface for everything reservation screen is allowed to show
sealed interface ReservationsState {
    // Reservations that are being fetched (aka loading.)
    data object Loading : ReservationsState

    /**
     * Responsible for making sure that reservations load successfully or if they are empty.
     *
     * @param selectedTab: the tab which the user currently clicked
     * @param items: Every reservation for this tab
     */
    data class Content(
        val selectedTab: ReservationTab = ReservationTab.Booked,
        val items: List<ReservationItem> = emptyList(),
    ) : ReservationsState {
        // Only the reservations belonging to the current tab (selectedTab)
        val visibleItems: List<ReservationItem>
            get() = items.filter { it.state in selectedTab.states }
    }

    // Could not load, send error
    data class Error(val message: String) : ReservationsState
    }

/**
 * Responsible for loading reservations that each mode can see, combines the campsite and turns
 * it into a card.
 *
 * @param campsiteRepository campsite information (mainly used for campsite name)
 * @param reservationRepository Reservation information
 * @param observeCurrentUser Signed-in userId
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
     * Tabs for the current user, updated whenever the signed-in user or their type changes.
     * Empty while no one is signed in.
     */
    val tabs: StateFlow<List<ReservationTab>> = observeCurrentUser()
        .map { user ->
            if (user == null) {
                emptyList()
            } else {
                when (user.userType) {
                    UserType.Entrant -> listOf(
                        ReservationTab.Booked,
                        ReservationTab.Booking,
                        ReservationTab.Past,
                        ReservationTab.Cancelled,
                    )

                    UserType.CampsiteOwner -> listOf(
                        ReservationTab.Booked,
                        ReservationTab.Booking,
                        ReservationTab.Past,
                        ReservationTab.Cancelled,
                    )

                    UserType.Admin -> listOf(
                        ReservationTab.Booked,
                        ReservationTab.Booking,
                        ReservationTab.Past,
                        ReservationTab.Cancelled,
                    )
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /**
     * Responsible for loading the screen. So if the state changes such as a
     * cancellation, these changes appear when coming back to reservations.
     */
    fun load() {

        viewModelScope.launch {

            // Only shows "Loading..." at the beginning
            if (_state.value !is ReservationsState.Content) _state.value = ReservationsState.Loading

            try {
                val user = observeCurrentUser().first()
                if (user == null) {
                    _state.value = ReservationsState.Error("Sign in to view reservations")
                    return@launch
                }

                val selectedTab = ReservationTab.Booked

                val allReservations = reservationRepository.getReservations().orEmpty()

                // Each mode decides which reservations to show
                val reservations = when (user.userType) {
                    UserType.Entrant -> allReservations.filter { it.userId == user.userId }
                    UserType.CampsiteOwner -> allReservations.filter {it.userId == user.userId ||
                            campsiteRepository.getCampsite(it.campsiteId)?.ownerUserId == user.userId}
                    UserType.Admin -> allReservations

                }.sortedByDescending { it.timestamp }

                val items = reservations.map { reservation ->
                    reservation.toItem(campsiteName = campsiteRepository.getCampsite(reservation.campsiteId)?.name,
                        canMakeReservation = reservation.userId == user.userId &&
                                reservation.reservationState == ReservationState.Pending,
                        )
                }

                _state.value = ReservationsState.Content(selectedTab = selectedTab, items = items)
            } catch (e: Exception) {
                _state.value = ReservationsState.Error("Unable to load reservations")
            }
        }
    }

    /**
     * Turning a reservation into a card
     *
     * @param campsiteName Name of campsite
     * @param canMakeReservation boolean for if we can make a reservation
     */
    private fun Reservation.toItem(campsiteName: String?, canMakeReservation: Boolean): ReservationItem {
        return ReservationItem(
            reservationId = reservationId,
            state = reservationState,
            title = campsiteName?.takeIf { it.isNotBlank() }?: "Campsite",
            dateText = daysAgoText(timestamp),
            canMakeReservation = canMakeReservation,
        )
    }

    /**
     * Switches tabs
     *
     * @param tab The tab the user clicked
     */
    fun selectTab(tab: ReservationTab) {
        _state.update { (it as? ReservationsState.Content)?.copy(selectedTab = tab) ?: it }
    }

    /**
     * Formatting how long ago a timestamp was
     *
     * @param epochSeconds time to format, in seconds
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